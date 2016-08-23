
//
// mon.c
//
// Copyright (c) 2012 TJ Holowaychuk <tj@vision-media.ca>
//

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <assert.h>
#include <string.h>
#include <fcntl.h>
#include <signal.h>
#include <stdint.h>
#include <sys/types.h>
#include <sys/time.h>
#include <sys/wait.h>
#include <sys/stat.h>
#include "commander.h"
#include "ms.h"

/*
 * Program version.
 */

#define VERSION "1.1.1"

/*
 * Log prefix.
 */

static const char *prefix = NULL;

/*
 * Monitor.
 */

typedef struct {
	const char *pidfile;
	const char *mon_pidfile;
	const char *logfile;
	const char *on_error;
	const char *on_restart;
	int64_t last_restart_at;
	int64_t clock;
	int daemon;
	int sleepsec;
	int max_attempts;
	int attempts;
} monitor_t;

/*
 * Logger.
 */

#define log(fmt, args...) \
  do { \
    if (prefix) { \
      printf("mon : %s : " fmt "\n", prefix, ##args); \
	    } else { \
      printf("mon : " fmt "\n", ##args); \
	    } \
    } while(0)

/*
 * Output error `msg`.
 */

void
error(char *msg) {
	fprintf(stderr, "Error: %s\n", msg);
	exit(1);
}

/*
 * Check if process of `pid` is alive.
 */

int
alive(pid_t pid) {
	return 0 == kill(pid, 0);
}

/*
 * Graceful exit, signal process group.
 */

void
graceful_exit(int sig) {
	pid_t pid = getpid();
	log("shutting down");
	log("kill(-%d, %d)", pid, sig);
	kill(-pid, sig);
	log("bye :)");
	exit(0);
}

/*
 * Return a timestamp in milliseconds.
 */

int64_t
timestamp() {
	struct timeval tv;
	int ret = gettimeofday(&tv, NULL);
	if (-1 == ret) return -1;
	return (int64_t) ((int64_t) tv.tv_sec * 1000 + (int64_t) tv.tv_usec / 1000);
}

/*
 * Write `pid` to `file`.
 */

void
write_pidfile(const char *file, pid_t pid) {
	char buf[32] = { 0 };
	snprintf(buf, 32, "%d", pid);
	int fd = open(file, O_WRONLY | O_CREAT, S_IRUSR | S_IWUSR);
	if (fd < 0) perror("open()");
	write(fd, buf, 32);
	close(fd);
}

/*
 * Output status of `pidfile`.
 */

void
show_status_of(const char *pidfile) {
	off_t size;
	struct stat s;

	// stat
	if (stat(pidfile, &s) < 0) {
		perror("stat()");
		exit(1);
	}

	size = s.st_size;

	// opens
	int fd = open(pidfile, O_RDONLY, 0);
	if (fd < 0) {
		perror("open()");
		exit(1);
	}

	// read
	char buf[size];
	if (size != read(fd, buf, size)) {
		perror("read()");
		exit(1);
	}

	// uptime
	time_t modified = s.st_mtime;

	struct timeval t;
	gettimeofday(&t, NULL);
	time_t now = t.tv_sec;
	time_t secs = now - modified;

	// status
	pid_t pid = atoi(buf);

	if (alive(pid)) {
		char *str = milliseconds_to_long_string(secs * 1000);
		printf("\e[90m%d\e[0m : \e[32malive\e[0m : uptime %s\e[m\n", pid, str);
		free(str);
	}
	else {
		printf("\e[90m%d\e[0m : \e[31mdead\e[0m\n", pid);
	}

	close(fd);
}

/*
 * Redirect stdio to `file`.
 */

void
redirect_stdio_to(const char *file) {
	int logfd = open(file, O_WRONLY | O_CREAT | O_APPEND, 0755);
	int nullfd = open("/dev/null", O_RDONLY, 0);

	if (-1 == logfd) {
		perror("open()");
		exit(1);
	}

	if (-1 == nullfd) {
		perror("open()");
		exit(1);
	}

	dup2(nullfd, 0);
	dup2(logfd, 1);
	dup2(logfd, 2);
}

/*
 * Daemonize the program.
 */

void
daemonize() {
	if (fork()) exit(0);

	if (setsid() < 0) {
		perror("setsid()");
		exit(1);
	}
}

/*
 * Invoke the --on-restart command.
 */

void
exec_restart_command(monitor_t *monitor) {
	log("on restart `%s`", monitor->on_restart);
	int status = system(monitor->on_restart);
	if (status) log("exit(%d)", status);
}

/*
 * Invoke the --on-error command.
 */

void
exec_error_command(monitor_t *monitor) {
	log("on error `%s`", monitor->on_error);
	int status = system(monitor->on_error);
	if (status) log("exit(%d)", status);
}

/*
 * Return the ms since the last restart.
 */

int64_t
ms_since_last_restart(monitor_t *monitor) {
	if (0 == monitor->last_restart_at) return 0;
	int64_t now = timestamp();
	return now - monitor->last_restart_at;
}

/*
 * Check if the maximum restarts within 60 seconds
 * have been exceeded and return 1, 0 otherwise.
 */

int
attempts_exceeded(monitor_t *monitor, int64_t ms) {
	monitor->attempts++;
	monitor->clock -= ms;

	// reset
	if (monitor->clock <= 0) {
		monitor->clock = 60000;
		monitor->attempts = 0;
		return 0;
	}

	// all good
	if (monitor->attempts < monitor->max_attempts) return 0;

	return 1;
}

/*
 * Monitor the given `cmd`.
 */

void
start(const char *cmd, monitor_t *monitor) {
exec: {
	pid_t pid = fork();
	int status;

	switch (pid) {
	case -1:
		perror("fork()");
		exit(1);
	case 0:
		signal(SIGTERM, SIG_DFL);
		signal(SIGQUIT, SIG_DFL);
		log("sh -c \"%s\"", cmd);
		execl("/bin/sh", "sh", "-c", cmd, 0);
		perror("execl()");
		exit(1);
	default:
		log("child %d", pid);

		// write pidfile
		if (monitor->pidfile) {
			log("write pid to %s", monitor->pidfile);
			write_pidfile(monitor->pidfile, pid);
		}

		// wait for exit
		waitpid(pid, &status, 0);

		// signalled
		if (WIFSIGNALED(status)) {
			log("signal(%s)", strsignal(WTERMSIG(status)));
			log("sleep(%d)", monitor->sleepsec);
			sleep(monitor->sleepsec);
			goto error;
		}

		// check status
		if (WEXITSTATUS(status)) {
			log("exit(%d)", WEXITSTATUS(status));
			log("sleep(%d)", monitor->sleepsec);
			sleep(monitor->sleepsec);
			goto error;
		}

		// restart
	error: {
		if (monitor->on_restart) exec_restart_command(monitor);
		int64_t ms = ms_since_last_restart(monitor);
		monitor->last_restart_at = timestamp();
		log("last restart %s ago", milliseconds_to_long_string(ms));
		log("%d attempts remaining", monitor->max_attempts - monitor->attempts);

		if (attempts_exceeded(monitor, ms)) {
			char *time = milliseconds_to_long_string(60000 - monitor->clock);
			log("%d restarts within %s, bailing", monitor->max_attempts, time);
			exec_error_command(monitor);
			log("bye :)");
			exit(2);
		}

		goto exec;
	}
	}
}
}

/*
 * --log <path>
 */

static void
on_log(command_t *self) {
	monitor_t *monitor = (monitor_t *) self->data;
	monitor->logfile = self->arg;
}

/*
 * --sleep <sec>
 */

static void
on_sleep(command_t *self) {
	monitor_t *monitor = (monitor_t *) self->data;
	monitor->sleepsec = atoi(self->arg);
}

/*
 * --daemonize
 */

static void
on_daemonize(command_t *self) {
	monitor_t *monitor = (monitor_t *) self->data;
	monitor->daemon = 1;
}

/*
 * --pidfile <path>
 */

static void
on_pidfile(command_t *self) {
	monitor_t *monitor = (monitor_t *) self->data;
	monitor->pidfile = self->arg;
}

/*
 * --mon-pidfile <path>
 */

static void
on_mon_pidfile(command_t *self) {
	monitor_t *monitor = (monitor_t *) self->data;
	monitor->mon_pidfile = self->arg;
}

/*
 * --status
 */

static void
on_status(command_t *self) {
	monitor_t *monitor = (monitor_t *) self->data;
	if (!monitor->pidfile) error("--pidfile required");
	show_status_of(monitor->pidfile);
	exit(0);
}

/*
 * --prefix
 */

static void
on_prefix(command_t *self) {
	monitor_t *monitor = (monitor_t *) self->data;
	prefix = self->arg;
}

/*
 * --on-restart <cmd>
 */

static void
on_restart(command_t *self) {
	monitor_t *monitor = (monitor_t *) self->data;
	monitor->on_restart = self->arg;
}

/*
 * --on-error <cmd>
 */

static void
on_error(command_t *self) {
	monitor_t *monitor = (monitor_t *) self->data;
	monitor->on_error = self->arg;
}

/*
 * --attempts <n>
 */

static void
on_attempts(command_t *self) {
	monitor_t *monitor = (monitor_t *) self->data;
	monitor->max_attempts = atoi(self->arg);
}

/*
 * [options] <cmd>
 */

int
main(int argc, char **argv){
	monitor_t monitor;
	monitor.pidfile = NULL;
	monitor.mon_pidfile = NULL;
	monitor.on_restart = NULL;
	monitor.on_error = NULL;
	monitor.logfile = "mon.log";
	monitor.daemon = 0;
	monitor.sleepsec = 1;
	monitor.max_attempts = 10;
	monitor.attempts = 0;
	monitor.last_restart_at = 0;
	monitor.clock = 60000;

	command_t program;
	command_init(&program, "mon", VERSION);
	program.data = &monitor;
	program.usage = "[options] <command>";
	command_option(&program, "-l", "--log <path>", "specify logfile [mon.log]", on_log);
	command_option(&program, "-s", "--sleep <sec>", "sleep seconds before re-executing [1]", on_sleep);
	command_option(&program, "-S", "--status", "check status of --pidfile", on_status);
	command_option(&program, "-p", "--pidfile <path>", "write pid to <path>", on_pidfile);
	command_option(&program, "-m", "--mon-pidfile <path>", "write mon(1) pid to <path>", on_mon_pidfile);
	command_option(&program, "-P", "--prefix <str>", "add a log prefix", on_prefix);
	command_option(&program, "-d", "--daemonize", "daemonize the program", on_daemonize);
	command_option(&program, "-a", "--attempts <n>", "retry attempts within 60 seconds [10]", on_attempts);
	command_option(&program, "-R", "--on-restart <cmd>", "execute <cmd> on restarts", on_restart);
	command_option(&program, "-E", "--on-error <cmd>", "execute <cmd> on error", on_error);
	command_parse(&program, argc, argv);

	// command required
	if (!program.argc) error("<cmd> required");
	const char *cmd = program.argv[0];

	// signals
	signal(SIGTERM, graceful_exit);
	signal(SIGQUIT, graceful_exit);

	// daemonize
	if (monitor.daemon) {
		daemonize();
		redirect_stdio_to(monitor.logfile);
	}

	// write mon pidfile
	if (monitor.mon_pidfile) {
		log("write mon pid to %s", monitor.mon_pidfile);
		write_pidfile(monitor.mon_pidfile, getpid());
	}

	start(cmd, &monitor);

	return 0;
}



//
// term.c
//
// Copyright (c) 2012 TJ Holowaychuk <tj@vision-media.ca>
//

#include <stdio.h>
#include <string.h>
#include <sys/ioctl.h>
#include "term.h"

/*
 * X pos.
 */

static int _x = 0;

/*
 * Y pos.
 */

static int _y = 0;

/*
 * Move to `(x, y)`.
 */

void
term_move_to(int x, int y) {
	_x = x;
	_y = y;
	printf("\e[%d;%d;f", y, x);
}

/*
 * Move by `(x, y)`.
 */

void
term_move_by(int x, int y) {
	term_move_to(_x + x, _y + y);
}

/*
 * Set `w` and `h` to the terminal dimensions.
 */

int
term_size(int *w, int *h) {
	struct winsize ws;
	int ret = ioctl(0, TIOCGWINSZ, &ws);
	if (ret < 0) return ret;
	*w = ws.ws_col;
	*h = ws.ws_row;
}

/*
 * Return the erase code for `name` or -1.
 */

const char *
term_erase_from_name(const char *name) {
	if (!strcmp("end", name)) return "K";
	if (!strcmp("start", name)) return "1K";
	if (!strcmp("line", name)) return "2K";
	if (!strcmp("up", name)) return "1J";
	if (!strcmp("down", name)) return "J";
	if (!strcmp("screen", name)) return "1J";
	return NULL;
}

/*
 * Erase with `name`:
 *
 *   - "end"
 *   - "start"
 *   - "line"
 *   - "up"
 *   - "down"
 *   - "screen"
 *
 */

int
term_erase(const char *name) {
	const char *s = term_erase_from_name(name);
	if (!s) return -1;
	printf("\e[%s", s);
	return 0;
}

/*
 * Return the color code for `name` or -1.
 */

int
term_color_from_name(const char *name) {
	if (!strcmp("black", name)) return 0;
	if (!strcmp("red", name)) return 1;
	if (!strcmp("green", name)) return 2;
	if (!strcmp("yellow", name)) return 3;
	if (!strcmp("blue", name)) return 4;
	if (!strcmp("magenta", name)) return 5;
	if (!strcmp("cyan", name)) return 6;
	if (!strcmp("white", name)) return 7;
	return -1;
}

/*
 * Set color by `name` or return -1.
 */

int
term_color(const char *name) {
	// TODO: refactor term_color_from_name()
	if (!strcmp("gray", name) || !strcmp("grey", name)) {
		printf("\e[90m");
		return 0;
	}

	int n = term_color_from_name(name);
	if (-1 == n) return n;
	printf("\e[3%dm", n);
	return 0;
}

/*
 * Set background color by `name` or return -1.
 */

int
term_background(const char *name) {
	int n = term_color_from_name(name);
	if (-1 == n) return n;
	printf("\e[4%dm", n);
	return 0;
}

//
// ms.c
//
// Copyright (c) 2012 TJ Holowaychuk <tj@vision-media.ca>
//

#include <stdlib.h>
#include <string.h>
#include <stdio.h>
#include "ms.h"

// microseconds

#define US_SEC 1000000
#define US_MIN 60 * US_SEC
#define US_HOUR 60 * US_MIN
#define US_DAY 24 * US_HOUR
#define US_WEEK 7 * US_HOUR
#define US_YEAR 52 * US_WEEK

// milliseconds

#define MS_SEC 1000
#define MS_MIN 60000
#define MS_HOUR 3600000
#define MS_DAY 86400000
#define MS_WEEK 604800000
#define MS_YEAR 31557600000

/*
 * Convert the given `str` representation to microseconds,
 * for example "10ms", "5s", "2m", "1h" etc.
 */

long long
string_to_microseconds(const char *str) {
	size_t len = strlen(str);
	long long val = strtoll(str, NULL, 10);
	if (!val) return -1;
	switch (str[len - 1]) {
	case 's': return  'm' == str[len - 2] ? val * 1000 : val * US_SEC;
	case 'm': return val * US_MIN;
	case 'h': return val * US_HOUR;
	case 'd': return val * US_DAY;
	case 'w': return val * US_WEEK;
	case 'y': return val * US_YEAR;
	default:  return val;
	}
}

/*
 * Convert the given `str` representation to milliseconds,
 * for example "10ms", "5s", "2m", "1h" etc.
 */

long long
string_to_milliseconds(const char *str) {
	size_t len = strlen(str);
	long long val = strtoll(str, NULL, 10);
	if (!val) return -1;
	switch (str[len - 1]) {
	case 's': return  'm' == str[len - 2] ? val : val * 1000;
	case 'm': return val * MS_MIN;
	case 'h': return val * MS_HOUR;
	case 'd': return val * MS_DAY;
	case 'w': return val * MS_WEEK;
	case 'y': return val * MS_YEAR;
	default:  return val;
	}
}

/*
 * Convert the given `str` representation to seconds.
 */

long long
string_to_seconds(const char *str) {
	long long ret = string_to_milliseconds(str);
	if (-1 == ret) return ret;
	return ret / 1000;
}

/*
 * Convert the given `ms` to a string. This
 * value must be `free()`d by the developer.
 */

char *
milliseconds_to_string(long long ms) {
	char *str = malloc(MS_MAX);
	if (!str) return NULL;
	long div = 1;
	char *fmt;

	if (ms < MS_SEC) fmt = "%lldms";
	else if (ms < MS_MIN) { fmt = "%llds"; div = MS_SEC; }
	else if (ms < MS_HOUR) { fmt = "%lldm"; div = MS_MIN; }
	else if (ms < MS_DAY) { fmt = "%lldh"; div = MS_HOUR; }
	else if (ms < MS_WEEK) { fmt = "%lldd"; div = MS_DAY; }
	else if (ms < MS_YEAR) { fmt = "%lldw"; div = MS_WEEK; }
	else { fmt = "%lldy"; div = MS_YEAR; }
	snprintf(str, MS_MAX, fmt, ms / div);

	return str;
}

/*
 * Convert the given `ms` to a long string. This
 * value must be `free()`d by the developer.
 */

char *
milliseconds_to_long_string(long long ms) {
	long div;
	char *name;

	char *str = malloc(MS_MAX);
	if (!str) return NULL;

	if (ms < MS_SEC) {
		sprintf(str, "less than one second");
		return str;
	}

	if (ms < MS_MIN) { name = "second"; div = MS_SEC; }
	else if (ms < MS_HOUR) { name = "minute"; div = MS_MIN; }
	else if (ms < MS_DAY) { name = "hour"; div = MS_HOUR; }
	else if (ms < MS_WEEK) { name = "day"; div = MS_DAY; }
	else if (ms < MS_YEAR) { name = "week"; div = MS_WEEK; }
	else { name = "year"; div = MS_YEAR; }

	long long val = ms / div;
	char *fmt = 1 == val
		? "%lld %s"
		: "%lld %ss";

	snprintf(str, MS_MAX, fmt, val, name);
	return str;
}

// tests

#ifdef TEST_MS

#include <assert.h>

void
equal(char *a, char *b) {
	if (strcmp(a, b)) {
		printf("expected: %s\n", a);
		printf("actual: %s\n", b);
		exit(1);
	}
}

void
test_string_to_microseconds() {
	assert(string_to_microseconds("") == -1);
	assert(string_to_microseconds("s") == -1);
	assert(string_to_microseconds("hey") == -1);
	assert(string_to_microseconds("5000") == 5000);
	assert(string_to_microseconds("1ms") == 1000);
	assert(string_to_microseconds("5ms") == 5000);
	assert(string_to_microseconds("1s") == 1000000);
	assert(string_to_microseconds("5s") == 5000000);
	assert(string_to_microseconds("1m") == 60000000);
	assert(string_to_microseconds("1h") == 3600000000);
	assert(string_to_microseconds("2d") == 2 * 24 * 3600000000);
}

void
test_string_to_milliseconds() {
	assert(string_to_milliseconds("") == -1);
	assert(string_to_milliseconds("s") == -1);
	assert(string_to_milliseconds("hey") == -1);
	assert(string_to_milliseconds("5000") == 5000);
	assert(string_to_milliseconds("1ms") == 1);
	assert(string_to_milliseconds("5ms") == 5);
	assert(string_to_milliseconds("1s") == 1000);
	assert(string_to_milliseconds("5s") == 5000);
	assert(string_to_milliseconds("1m") == 60 * 1000);
	assert(string_to_milliseconds("1h") == 60 * 60 * 1000);
	assert(string_to_milliseconds("1d") == 24 * 60 * 60 * 1000);
}

void
test_string_to_seconds() {
	assert(string_to_seconds("") == -1);
	assert(string_to_seconds("s") == -1);
	assert(string_to_seconds("hey") == -1);
	assert(string_to_seconds("5000") == 5);
	assert(string_to_seconds("1ms") == 0);
	assert(string_to_seconds("5ms") == 0);
	assert(string_to_seconds("1s") == 1);
	assert(string_to_seconds("5s") == 5);
	assert(string_to_seconds("1m") == 60);
	assert(string_to_seconds("1h") == 60 * 60);
	assert(string_to_seconds("1d") == 24 * 60 * 60);
}

void
test_milliseconds_to_string() {
	equal("500ms", milliseconds_to_string(500));
	equal("5s", milliseconds_to_string(5000));
	equal("2s", milliseconds_to_string(2500));
	equal("1m", milliseconds_to_string(MS_MIN));
	equal("5m", milliseconds_to_string(5 * MS_MIN));
	equal("1h", milliseconds_to_string(MS_HOUR));
	equal("2d", milliseconds_to_string(2 * MS_DAY));
	equal("2w", milliseconds_to_string(15 * MS_DAY));
	equal("3y", milliseconds_to_string(3 * MS_YEAR));
}

void
test_milliseconds_to_long_string() {
	equal("less than one second", milliseconds_to_long_string(500));
	equal("5 seconds", milliseconds_to_long_string(5000));
	equal("2 seconds", milliseconds_to_long_string(2500));
	equal("1 minute", milliseconds_to_long_string(MS_MIN));
	equal("5 minutes", milliseconds_to_long_string(5 * MS_MIN));
	equal("1 hour", milliseconds_to_long_string(MS_HOUR));
	equal("2 days", milliseconds_to_long_string(2 * MS_DAY));
	equal("2 weeks", milliseconds_to_long_string(15 * MS_DAY));
	equal("1 year", milliseconds_to_long_string(MS_YEAR));
	equal("3 years", milliseconds_to_long_string(3 * MS_YEAR));
}

int
main(){
	test_string_to_microseconds();
	test_string_to_milliseconds();
	test_string_to_seconds();
	test_milliseconds_to_string();
	test_milliseconds_to_long_string();
	printf("\n  \e[32m\u2713 \e[90mok\e[0m\n\n");
	return 0;
}

#endif
