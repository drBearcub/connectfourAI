import java.util.*;
import java.lang.Math;

/*
class node
{
	public node(GameStateModule game) {
		m_GameState = game.copy();
		isZombie = false;
		children = new ArrayList<node>();
		//Moves = new ArrayList<Integer>();
	}
	public node() { //zombie constructor
		isZombie = true;
		children = new ArrayList<node>();
	}
	//public double score;
	public GameStateModule m_GameState;
	public ArrayList<node> children;
	public boolean isZombie;
	//public ArrayList<Integer> Moves;
}
*/

public class AngelaAI extends AIModule 
{
	public int ourPlayer = -1;
	public int gameWidth = -1;
	public int gameHeight = -1;
	public int theirPlayer = -1;
	public int depthLimit = 1;
	public GameStateModule m_GameState;

	public void initialize(final GameStateModule game) {
		ourPlayer = game.getActivePlayer();
		gameWidth = game.getWidth();
		gameHeight = game.getHeight();
		m_GameState = game;
		if(ourPlayer == 1) {
			theirPlayer = 2;
		} else {
			theirPlayer = 1;
		}
		depthLimit = 7;
		chosenMove = -1;
	}

	public void println(String s){
		System.out.println(s);
	}

	public void getNextMove(GameStateModule game) {
		final long startTime = System.currentTimeMillis();
		initialize(game);
		println("in getNextMove");

		betterPruning(game, 0, true);
		//betterMinimax(0, true);
		//abPruning(-99999, 99999, 0, true);

		final long endTime = System.currentTimeMillis();
		System.out.println("Total execution time: " + (endTime - startTime) );
	}

	public int canWinHorizontally(GameStateModule game, int player){
		int counter = 0;
		int playerCounter = 0;
		int emptyCounter = 0;

		for(int i = 0; i < gameHeight; ++i){
			for(int j = 0; j < gameWidth-3; ++j){
				for(int k = 0; k < 4; ++k){
					if(game.getAt(j+k, i) == player){
						++playerCounter;
					} else if(game.getAt(j+k, i) == 0){
						++emptyCounter;
						//println("emptyCounter: "+emptyCounter);
					}
					//System.out.print(" x: "+(j+k)+" y: "+ i+" player: "+game.getAt(j+k,i));
				}
				//println("playerCounter: "+playerCounter+" emptyCounter: " +emptyCounter);
				if(playerCounter == 3 && emptyCounter == 1) {
					++counter;
					//println("playerCounter: "+playerCounter+" emptyCounter: "+emptyCounter);
				}
				playerCounter = 0;
				emptyCounter = 0;
			}
		}
		//println("horizontal counter: "+counter);
		return counter;
	}

	public int canWinVertically(GameStateModule game, int player){
		int counter = 0;
		int playerCounter = 0;
		int colomnHeight;
		for(int i = 0; i < gameWidth; ++i){
			colomnHeight = game.getHeightAt(i);
			//println(colomnHeight +"");
			if(colomnHeight > 2 && colomnHeight < gameHeight){
				for(int j = 0; j < 3; ++j){
					if(game.getAt(i, colomnHeight-j) == player) {
						++playerCounter;
					}
				}
				if(playerCounter == 3) {
					++counter;
				}
				playerCounter = 0;
			}
		}
		//println("vertical counter " + counter);
		return counter;
	}

	public int defensiveCheck(GameStateModule game) {
		return 100 * (canWinHorizontally(game, theirPlayer) + canWinVertically(game, theirPlayer));
	}

	public int offensiveCheck(GameStateModule game) {
		return 100 * (canWinHorizontally(game, ourPlayer) + canWinVertically(game, ourPlayer));
	}


	public double evaluation(GameStateModule game) {
		if(game.isGameOver()) {
			if(ourPlayer == game.getWinner()) {
				//println("win");
				return 9999;
			}else if(ourPlayer != game.getWinner()){
				//println("lose");
				return -9999;
			} else if (game.getWinner() == 0) {
				//rintln("draw");
				return 0;
			} 
		}

/*
		int defensivePoint = defensiveCheck(game);
		
		if(defensivePoint > 0) {
			return defensivePoint * -1;
		} 

		int offensivePoint = offensiveCheck(game);

		if(offensivePoint > 0) {
			return offensivePoint;
		}
		*/

		return 0;
	}

	public double abPruning(double alpha, double beta, int level, boolean maxPlay) {
		double curVal;
		double curAlpha = alpha;
		double curBeta = beta;

		if(m_GameState.isGameOver() || level == depthLimit) {
			println("terminal node");
			return evaluation(m_GameState);
		}
		if(maxPlay) {
			curVal = -99999;
			for(int i = 0; i < gameWidth; ++i) {
				if(m_GameState.canMakeMove(i)) {
					m_GameState.makeMove(i);
					curVal = Math.max(curVal, abPruning(curAlpha, curBeta, level + 1, false));
					if(curAlpha < curVal) {
						curAlpha = curVal;
						if(level == 0) {
							chosenMove = i;
						}
					}
					m_GameState.unMakeMove();
					if(curBeta <= curAlpha) {
						break;
					}
				}
			}
			return curVal;	
		} else {
			curVal = 99999;
			for(int i = 0; i < gameWidth; ++i) {
				if(m_GameState.canMakeMove(i)) {
					m_GameState.makeMove(i);
					curVal = Math.min(curVal, abPruning(curAlpha, curBeta, level + 1, true));
					curBeta = Math.min(curBeta, curVal);
					m_GameState.unMakeMove();
					if(curBeta <= curAlpha) {
						break;
					}
				}
			}
			return curVal;
		}
	}

	public double betterMinimax(int level, boolean maxPlay) {
		//println("in betterminiax, level: " + level);
		double bestValue = 0;
		double curVal;
		if(m_GameState.isGameOver() || level == depthLimit) {
			//println("terminal node");
			return evaluation(m_GameState);
		}
		if(maxPlay) {
			bestValue = -99999;
			for(int i = 0; i < gameWidth; ++i) {
				if(m_GameState.canMakeMove(i)) {
					m_GameState.makeMove(i);
					curVal = betterMinimax(level + 1, false);
					if(bestValue < curVal) {
						bestValue = curVal;
						if(level == 0) {
							chosenMove = i;
						}
					}
					m_GameState.unMakeMove();
				}
			}
			return bestValue - level;
		} else { // minPlay
			bestValue = 99999;
			for(int i = 0; i < gameWidth; ++i) {
				if(m_GameState.canMakeMove(i)) {
					m_GameState.makeMove(i);
					curVal = betterMinimax(level + 1, true);
					bestValue = Math.min(bestValue, curVal);
					m_GameState.unMakeMove();
				}
			}
			return bestValue + level;
		}
	}

	public double betterPruning(GameStateModule state, int level, boolean maxPlay) {
		double bestValue;
		double curVal;
		if(level == depthLimit) {
			return evaluation(m_GameState);
		}
		if(maxPlay) {
			int winCount = 0;
			for(int i = 0; i < gameWidth; ++i) {
				if(state.canMakeMove(i)) {
					state.makeMove(i);
					if(state.isGameOver()) {
						if(state.getWinner() == ourPlayer) {
							++winCount;
							if(level == 0) {
								chosenMove = i;
								return 0;
							}
						}
					}
					state.unMakeMove();
				}
			}
			if(winCount > 0) {
				return winCount*100; // becomes terminal node
			} 

			else {
				bestValue = Double.NEGATIVE_INFINITY;
				for(int i = 0; i < gameWidth; ++i) {
					if(state.canMakeMove(i)) {
						GameStateModule copy = state.copy(); // might not be necessary
						copy.makeMove(i);
						curVal = betterPruning(copy, level + 1, false);
						if(bestValue < curVal) {
							bestValue = curVal;
							if(level == 0) {
								chosenMove = i;
							}
						}
					}
				}
				return bestValue - level;
			}
		} else {
			int loseCount = 0;
			for(int i = 0; i < gameWidth; ++i) {
				if(state.canMakeMove(i)) {
					state.makeMove(i);
					if(state.isGameOver()) {
						if(state.getWinner() == theirPlayer) {
							++loseCount;
						}
					}
					state.unMakeMove();
				}
			}
			if(loseCount > 0) {
				return loseCount*-100; // becomes terminal node
			} 

			else {
				bestValue = Double.POSITIVE_INFINITY;
				for(int i = 0; i < gameWidth; ++i) {
					if(state.canMakeMove(i)) {
						GameStateModule copy = state.copy(); // might not be necessary
						copy.makeMove(i);
						curVal = betterPruning(copy, level + 1, true);
						bestValue = Math.min(bestValue, curVal);
					}
				}
				return bestValue + level;
			}
		}
	}
}


