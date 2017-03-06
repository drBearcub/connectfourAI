import java.util.*;
import java.lang.Math;
import java.util.Random;

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
}*/

public class HaowenAI extends AIModule 
{
	public int ourPlayer = -1;
	public int gameWidth = -1;
	public int gameHeight = -1;

	public void initialize(final GameStateModule game) {
		ourPlayer = game.getActivePlayer();
		gameWidth = game.getWidth();
		//gameHeight = game.getHeight();
		chosenMove = -1;
	}

	public void println(String s){
		System.out.println(s);
	}

	public void getNextMove(final GameStateModule game) {
		final long startTime = System.currentTimeMillis();
		initialize(game);
		//println("in getNextMove");
		ArrayList<node> curGeneration = new ArrayList<node>();
		ArrayList<node> nextGeneration = new ArrayList<node>();
		node root = new node(game);

		curGeneration.add(root);

		int levels = 40;
		while(levels > 0)
		{
			for(int i = 0; i < curGeneration.size(); i++) {
				GameStateModule gameCopy_state = curGeneration.get(i).m_GameState.copy();
				if(!gameCopy_state.isGameOver()) {
					int zombieCount = 0;
					for(int j = 0; j < gameWidth; j++) {
						GameStateModule cur = gameCopy_state.copy();
						if(cur.canMakeMove(j)) {
							cur.makeMove(j);
							node curNode = new node(cur);
							curGeneration.get(i).children.add(curNode);
							nextGeneration.add(curNode);
							//println("can make move " + j);
						} else {
							zombieCount++;
							node zombie = new node();
							curGeneration.get(i).children.add(zombie);
							//println("zombie " + j);
						}
						/*
						if(zombieCount == gameWidth) {
							println("BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAD");
						}*/
					}
				} else {
					println("error check");
				}
			}
			curGeneration = (ArrayList)nextGeneration.clone();
			nextGeneration.clear();		
			levels--;
		}

		double testValue = minimax(root, 0, true);
		final long endTime = System.currentTimeMillis();
		System.out.println("Total execution time: " + (endTime - startTime) );
	}

	public int canWinHorizontally(GameStateModule game, int player)
	{
		int counter = 0;
		int playerCounter = 0;
		int emptyCounter = 0;

		for(int i = 0; i < gameHeight; i++)
		{
			for(int j = 0; j < game.getWinner()-4;j++)
			{
				for(int k = 0; k < 4; k++)
				{
					if(game.getAt(i,j+k) == player)
					{
						playerCounter++;
					}
					else if(game.getAt(i,j+k) == 0)
					{
						emptyCounter++;
					}
				}
				if(playerCounter==3 && emptyCounter ==1)
				{
					counter++;
				}
				playerCounter = 0;
				emptyCounter = 0;
			}
		}
		return counter;
	}

	public int canWinVertically(GameStateModule game, int player)
	{
		int counter = 0;
		int playerCounter = 0;

		for(int i = 0; i <gameWidth; i++)
		{
			for(int k = 1; k<4;k++)
			{
				if(game.getHeightAt(i)-k == player)
				{
					println("getheightat="+game.getHeightAt(i));
					playerCounter++;
				}
			}
			if(playerCounter == 3 && game.getHeightAt(i) == 0)
			{
				counter++;
			}
			playerCounter=0;
		}
		println("vertical counter"+counter);
		return counter;
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
			} else {

				//return canWinHorizontally(game,ourPlayer)+canWinVertically(game,ourPlayer);
				return 0;
			}
		}
		Random rand = new Random();
		int randomNum = rand.nextInt((7 - 0) + 1) + 0;
		return 1;
	}

	public double minimax(node state, int level, boolean maxPlay) {// need to accomodate for zombie
		//println("in minimax");
		double bestValue = 0;
		double v = 0;
		//println("number of children: " + state.children.size()+" at level: " + level);
		//println(state.m_GameState.isGameOver() + "");
		/*
		if(state.isZombie == true && maxPlay) {
			println("shouldnt be here1");
			return -99998;
		}
		if(state.isZombie == true && !maxPlay) {
			println("shouldnt be here2");
			return 99999;
		}
		*/
		if(state.children.size() == 0) {// shouldnt need level == 4
			double eval = evaluation(state.m_GameState);
			//println("level " +level);
			return eval;
		}
		if(maxPlay) {
			bestValue = -99999; // a valid state with only zombie children will trigger this scenario
			int zombieCount = 0;
			for(int i = 0; i < gameWidth; ++i) {
				if(!state.children.get(i).isZombie) {
					println("in child " +i);
					v = minimax(state.children.get(i), level + 1, false);
					println("new value " + v);
					if(bestValue < Math.max(bestValue, v)) { // cant let zombie trigger chosenmove
						//println("in update chosenMove");
						bestValue = Math.max(bestValue, v);
						if(level == 0) {
							chosenMove = i;		
							println("updating chosenMove " + i);
							println("best value is " + bestValue);
						}
					}
				} else {
					zombieCount++;
					//println("is zombie");
				}
			}
		/*	
			if(zombieCount == gameWidth) {
				println("BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAADYOOOOOOOOOOOOOOOOOOOOOOOO");
			}
		*/
			if(bestValue == -99999) {
				println("shouldnt be here max " + level);
				//chosenMove = 10;
				//return -9999;
				bestValue = evaluation(state.m_GameState);
			}
			bestValue -= level;
		} else {
			bestValue = 99998;
			for(int i = 0; i < gameWidth; ++i) {
				if(!state.children.get(i).isZombie) {
					v = minimax(state.children.get(i), level + 1, true);
					bestValue = Math.min(bestValue, v);
				} else {
					//println("is zombie");
				}
			}
			if(bestValue == 99998) {
				println("shouldnt be here min " + level);
				bestValue = evaluation(state.m_GameState);
			}
			bestValue += level;
		}
		//println("end minimax");
		return bestValue;
	}
}