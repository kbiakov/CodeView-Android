using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Reflection;
using CodeClassifier.StringTokenizer;

namespace CodeClassifier
{
	public class CodeClassifier
	{
		private static CodeClassifier _instance;

		private const double SCORE_MULTIPLIER_PER_LEVEL = 2;
		private const double SCORE_MULTIPLIER_FOR_EXACT_MATCH = 5;

		private static List<MatchTree> _matchTrees;

		private CodeClassifier()
		{
			string trainingSetPath = Path.GetDirectoryName(Assembly.GetExecutingAssembly().Location);
			if (trainingSetPath == null)
			{
				throw new DirectoryNotFoundException("Could not find the training-set folder.");
			}

			// Train classifier
			string path = Path.Combine(trainingSetPath, "training-set");
			string[] folders = Directory.GetDirectories(path);
			foreach (string folder in folders)
			{
				string[] files = Directory.GetFiles(folder);
				_matchTrees = new List<MatchTree>();
				foreach (string filePath in files)
				{
					string languageName = Path.GetFileNameWithoutExtension(filePath);
					if (languageName != null)
					{
						// Calculate the total possible score to normalize the score results
						double totalPossibleScore;
						TokenNode rootNode = BuildMatchTree(File.ReadAllText(filePath), out totalPossibleScore);
						_matchTrees.Add(new MatchTree(rootNode, languageName, totalPossibleScore));
					}
				}
			}
		}

		private static TokenNode BuildMatchTree(string trainingCode, out double totalScorePossible)
		{
			List<Token> tokens = GetAllTokens(trainingCode);

			// Recursivly build the tree
			TokenNode root = new TokenNode(TokenKind.Unknown, 0, 1, null);
			double totalScore = 0;
			for (int index = 0; index < tokens.Count-1; index++)
			{
				totalScore += AddTokens(root, tokens, index);
			}

			totalScorePossible = totalScore;
			return root;
		}

		private static double AddTokens(TokenNode tokenNode, IList<Token> tokens, int index)
		{
			double totalScore = 0;
			while (index < tokens.Count && tokenNode.Level < 10)
			{
				Token codeToken = tokens[index];
				TokenNode nextTreeToken = tokenNode.NextTokens.FirstOrDefault(nt => nt.Kind == codeToken.Kind);
				if (nextTreeToken == null)
				{
					// Token doesn't exist on this tree level yet
					var newToken = new TokenNode(codeToken.Kind, tokenNode.Level + 1, tokenNode.Score * SCORE_MULTIPLIER_PER_LEVEL, codeToken.Value);
					totalScore += tokenNode.Score * SCORE_MULTIPLIER_PER_LEVEL;
					tokenNode.NextTokens.Add(newToken);
					tokenNode = newToken;
				}
				else
				{
					// Token already exists on this level
					nextTreeToken.Examples.Add(codeToken.Value);
					tokenNode = nextTreeToken;
				}
				index++;
			}
			return totalScore;
		}

		private static List<Token> GetAllTokens(string code)
		{
			StringTokenizer.StringTokenizer stringTokenizer = new StringTokenizer.StringTokenizer(code);

			List<Token> tokens = new List<Token>();
			Token token;
			do
			{
				token = stringTokenizer.Next();
				tokens.Add(token);
			} while (token.Kind != TokenKind.Eof);
			return tokens;
		}

		public static string Classify(string snippet  )
		{
			// ReSharper disable once RedundantAssignment
			Dictionary<string, double> scores;
			return Classify(snippet, out scores);
		}

		public static string Classify(string snippet, out Dictionary<string, double> scores  )
		{
			if (_instance == null)
			{
				_instance = new CodeClassifier();
			}

			scores = new Dictionary<string, double>();

			List<Token> tokens = GetAllTokens(snippet);
			double maxScore = 0;
			string bestMatchLanguage = null;

			foreach (MatchTree matchTree in _matchTrees)
			{
				double score = 0;
				for (int index = 0; index < tokens.Count; index++)
				{
					score += ScoreTokens(matchTree.MatchTreeRoot, tokens, index);
				}
				score = score / tokens.Count() / matchTree.TotalPossibleScore;

				//Console.WriteLine(matchTree.Language + "\t" + score);
				scores.Add(matchTree.Language, score);
				if (score > maxScore)
				{
					maxScore = score;
					bestMatchLanguage = matchTree.Language;
				}
			}
			return bestMatchLanguage;
		}

		private static double ScoreTokens(TokenNode tokenNode, IList<Token> tokens, int index)
		{
			Token codeToken = tokens[index];
			TokenNode nextToken = tokenNode.NextTokens.FirstOrDefault(nt => nt.Kind == codeToken.Kind);
			if (nextToken != null)
			{
				// Token exists in match tree => points !!!
				double score = nextToken.Examples.Contains(codeToken.Value) ?
									SCORE_MULTIPLIER_FOR_EXACT_MATCH:
									SCORE_MULTIPLIER_PER_LEVEL;

				if (index < tokens.Count() - 1)
				{
					return score * ScoreTokens(nextToken, tokens, index + 1);
				}
				return score;
			}
			// Token did not exist => no points
			return 1;
		}
	}
}