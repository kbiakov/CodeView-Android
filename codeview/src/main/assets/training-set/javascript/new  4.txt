Object.extend(String.prototype, {
  // if a string doesn't end with str it appends it
  ensureEndsWith: function(str) {
    return this.endsWith(str) ? this : this + str;
  },
  
  // makes sure that string ends with px (for setting widths and heights)
  px: function() {
    return this.ensureEndsWith('px');
  }
});

Object.extend(Number.prototype, {
  // makes sure that number ends with px (for setting widths and heights)
  px: function() {
    return this.toString().px();
  }
});

var Window = {
  // returns correct dimensions for window, had issues with prototype's sometimes. this was ganked from apple.
  size: function() {
		var width  = window.innerWidth || (window.document.documentElement.clientWidth || window.document.body.clientWidth);
		var height = window.innerHeight || (window.document.documentElement.clientHeight || window.document.body.clientHeight);
		var x      = window.pageXOffset || (window.document.documentElement.scrollLeft || window.document.body.scrollLeft);
		var y      = window.pageYOffset || (window.document.documentElement.scrollTop || window.document.body.scrollTop);
		return {'width':width, 'height':height, 'x':x, 'y':y}
	}
}

var FancyZoomBox = {
  directory : 'images',
  zooming   : false,
  setup     : false,
  
  init: function(directory) {
    if (FancyZoomBox.setup) return;
    FancyZoomBox.setup = true;
    
    var ie = navigator.userAgent.match(/MSIE\s(\d)+/);
    if (ie) {
      var version = parseInt(ie[1]);
      Prototype.Browser['IE' + version.toString()] = true;
      Prototype.Browser.ltIE7 = (version < 7) ? true : false;
    }
    
    var html = '<div id="zoom" style="display:none;"> \
                  <table id="zoom_table" style="border-collapse:collapse; width:100%; height:100%;"> \
                    <tbody> \
                      <tr> \
                        <td class="tl" style="background:url(' + FancyZoomBox.directory + '/tl.png) 0 0 no-repeat; width:20px; height:20px; overflow:hidden;" /> \
                        <td class="tm" style="background:url(' + FancyZoomBox.directory + '/tm.png) 0 0 repeat-x; height:20px; overflow:hidden;" /> \
                        <td class="tr" style="background:url(' + FancyZoomBox.directory + '/tr.png) 100% 0 no-repeat; width:20px; height:20px; overflow:hidden;" /> \
                      </tr> \
                      <tr> \
                        <td class="ml" style="background:url(' + FancyZoomBox.directory + '/ml.png) 0 0 repeat-y; width:20px; overflow:hidden;" /> \
                        <td class="mm" style="background:#fff; vertical-align:top; padding:10px;"> \
                          <div id="zoom_content"> \
                          </div> \
                        </td> \
                        <td class="mr" style="background:url(' + FancyZoomBox.directory + '/mr.png) 100% 0 repeat-y;  width:20px; overflow:hidden;" /> \
                      </tr> \
                      <tr> \
                        <td class="bl" style="background:url(' + FancyZoomBox.directory + '/bl.png) 0 100% no-repeat; width:20px; height:20px; overflow:hidden;" /> \
                        <td class="bm" style="background:url(' + FancyZoomBox.directory + '/bm.png) 0 100% repeat-x; height:20px; overflow:hidden;" /> \
                        <td class="br" style="background:url(' + FancyZoomBox.directory + '/br.png) 100% 100% no-repeat; width:20px; height:20px; overflow:hidden;" /> \
                      </tr> \
                    </tbody> \
                  </table> \
                  <a href="#" title="Close" id="zoom_close" style="position:absolute; top:0; left:0;"> \
                    <img src="' + FancyZoomBox.directory + '/closebox.png" alt="Close" style="border:none; margin:0; padding:0;" /> \
                  </a> \
                </div>';
    
    var body  = $$('body').first();
    body.insert(html);
    
    FancyZoomBox.zoom = $('zoom');
    FancyZoomBox.zoom_table = $('zoom_table');
    FancyZoomBox.zoom_close = $('zoom_close');
    FancyZoomBox.zoom_content = $('zoom_content');
    FancyZoomBox.zoom_close.observe('click', FancyZoomBox.hide);
    FancyZoomBox.middle_row = $A([$$('td.ml'), $$('td.mm'), $$('td.mr')]).flatten();
    FancyZoomBox.cells = FancyZoomBox.zoom_table.select('td');
    
    // hide zoom if click fired is not inside zoom
    $$('html').first().observe('click', function(e) {
      var click_in_zoom = e.findElement('#zoom'),
          zoom_display  = FancyZoomBox.zoom.getStyle('display');
      if (zoom_display == 'block' && !click_in_zoom) {
        FancyZoomBox.hide(e);
      }
    });

    // esc to close zoom box
    $(document).observe('keyup', function(e) {
      var zoom_display = FancyZoomBox.zoom.getStyle('display');
      if (e.keyCode == Event.KEY_ESC && zoom_display == 'block') {
        FancyZoomBox.hide(e);
      }
    });
    
    // just use gifs as ie6 and below suck
    if (Prototype.Browser.ltIE7) {
      FancyZoomBox.switchBackgroundImagesTo('gif');
    }    
  },
  
  show: function(e) {
    e.stop();
		if (FancyZoomBox.zooming) return;
		FancyZoomBox.zooming   = true;
		var element            = e.findElement('a');
		var related_div        = element.content_div;
		var width              = (element.zoom_width || related_div.getWidth()) + 60;
		var height             = (element.zoom_height || related_div.getHeight()) + 60;
		var d                  = Window.size();
		var yOffset            = document.viewport.getScrollOffsets()[1];
		// ensure that newTop is at least 0 so it doesn't hide close button
		var newTop             = Math.max((d.height/2) - (height/2) + yOffset, 0);
		var newLeft            = (d.width/2) - (width/2);
		FancyZoomBox.curTop    = e.pointerY();
		FancyZoomBox.curLeft   = e.pointerX();
		FancyZoomBox.moveX     = -(FancyZoomBox.curLeft - newLeft);
		FancyZoomBox.moveY     = -(FancyZoomBox.curTop - newTop);
    FancyZoomBox.zoom.hide().setStyle({
			position	: 'absolute',
			top				: FancyZoomBox.curTop.px(),
			left			: FancyZoomBox.curLeft.px()
		});
    
		new Effect.Parallel([
			new Effect.Appear(FancyZoomBox.zoom, {sync:true}),
			new Effect.Move(FancyZoomBox.zoom, {x: FancyZoomBox.moveX, y: FancyZoomBox.moveY, sync: true}),
			new Effect.Morph(FancyZoomBox.zoom, {
			  style: {
			    width: width.px(),
			    height: height.px()
			  },
				sync: true,
				beforeStart: function(effect) {
				  // middle row height must be set for IE otherwise it tries to be "logical" with the height
    		  if (Prototype.Browser.IE) {
    		    FancyZoomBox.middle_row.invoke('setStyle', {height:(height-40).px()});
    		  }
					FancyZoomBox.fixBackgroundsForIE();
				},
				afterFinish: function(effect) {
				  FancyZoomBox.zoom_content.innerHTML = related_div.innerHTML;
					FancyZoomBox.unfixBackgroundsForIE();
					FancyZoomBox.zoom_close.show();
					FancyZoomBox.zooming = false;
				}
			})
		], { duration: 0.5 });
  },
  
  hide: function(e) {
    e.stop();
		if (FancyZoomBox.zooming) return;
		FancyZoomBox.zooming = true;		
		new Effect.Parallel([
			new Effect.Move(FancyZoomBox.zoom, {x: FancyZoomBox.moveX*-1, y: FancyZoomBox.moveY*-1, sync: true}),
			new Effect.Morph(FancyZoomBox.zoom, {
			  style: {
			    width: '1'.px(),
			    height: '1'.px()
			  },
				sync					: true,
				beforeStart: function(effect) {
					FancyZoomBox.fixBackgroundsForIE();
					FancyZoomBox.zoom_content.innerHTML = '';
					FancyZoomBox.zoom_close.hide();
				},
				afterFinish: function(effect) {
					FancyZoomBox.unfixBackgroundsForIE();
					FancyZoomBox.zooming = false;
				}
			}),
			new Effect.Fade(FancyZoomBox.zoom, {sync:true})
		], { duration: 0.5 });
  },
  
  // switches the backgrounds of the cells and the close image to png's or gif's
  // fixes ie's issues with fading and appearing transparent png's with 
  // no background and ie6's craptacular handling of transparent png's
  switchBackgroundImagesTo: function(to) {
    FancyZoomBox.cells.each(function(td) {
      var bg = td.getStyle('background-image').gsub(/\.(png|gif|none)\)$/, '.' + to + ')');
      td.setStyle('background-image: ' + bg);
    });
    var close_img = FancyZoomBox.zoom_close.firstDescendant();
    var new_img = close_img.readAttribute('src').gsub(/\.(png|gif|none)$/, '.' + to);
    close_img.writeAttribute('src', new_img);
  },
  
  // prevents the thick black border that happens when appearing or fading png in IE
	fixBackgroundsForIE: function() {
    if (Prototype.Browser.IE7) { FancyZoomBox.switchBackgroundImagesTo('gif'); }
	},
	
	// swaps back to png's for prettier shadows
	unfixBackgroundsForIE: function() {
    if (Prototype.Browser.IE7) { FancyZoomBox.switchBackgroundImagesTo('png'); }
	}
}

var FancyZoom = Class.create({
	initialize: function(element) {
	  this.options = arguments.length > 1 ? arguments[1] : {};
	  FancyZoomBox.init();
	  this.element = $(element);
		if (this.element) {
		  this.element.content_div = $(this.element.readAttribute('href').gsub(/^#/, ''));
  		this.element.content_div.hide();
  		this.element.zoom_width = this.options.width;
  		this.element.zoom_height = this.options.height;
      this.element.observe('click', FancyZoomBox.show);
		}
	}
});