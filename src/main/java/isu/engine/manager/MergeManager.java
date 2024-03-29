package isu.engine.manager;

import isu.engine.*;
import isu.util.CircularlyLinkedList;

import java.util.ArrayList;
import java.util.List;

public class MergeManager {

    private Tile t;
    private ArrayList<HotelChain> mergingChains;
    private ArrayList<HotelChain> largestChains;
    private HotelChain survivingChain;
    private GameEngine gameEngine;

    public MergeManager(GameEngine gameEngine) {
        this.gameEngine = gameEngine;
    }

    /**
     * Checks if there is a merge based on the placed tile. Starts the merge process
     * if a surviving chain can be determined without the player.
     * @param t
     * @return True if there is a merge
     */
    public boolean checkMerge(Tile t){
        this.t = t;
        Board b = gameEngine.getBoard();
        BoardCell[] surroundingCells = new BoardCell[4];
        List<Tile> neighboringTiles = b.getNeighboringTiles(t);
        for (Tile tile : neighboringTiles){
            if (tile.getChain() != null){
                neighboringTiles.remove(tile);
            }
        }
        findMergingChains(t);

        if (mergingChains.size() == 0){
            //check to see if a chain needs to be created
            if (neighboringTiles.size() > 0){
                //create new chain with tiles t and neighboring tiles

            }
        } else if (mergingChains.size() == 1){
            //add tile to chain if it is the only chain touching the tile
            mergingChains.get(0).addTile(t);

            //add extra surrounding tiles
            if (neighboringTiles.size() > 0) {
                mergingChains.get(0).addTiles(neighboringTiles);
            }
        } else {
            findSurvivingChain(mergingChains);
            if (survivingChain != null){
                //only starts the merge if the surviving chain is found automatically
                merge();
            }

            return true;
        }

        return false;
    }

    /**
     * Combines the chains, pays the bonuses to the players, and has each player sell/trade/keep there stocks
     */
    public void merge(){

        //add tile to surviving chain
        survivingChain.addTile(t);

        for (HotelChain chain : mergingChains){
            if (chain != survivingChain){
                //add other chains' tiles to surviving chain
                survivingChain.addTiles(chain.getTiles());
                chain.clearTiles();

                //pay bonuses
                GameEngine ge = gameEngine;
                ge.getBank().payBonus(chain, ge.getPlayers());
            }
        }

        //add extra surrounding tiles to chain
        Board b = gameEngine.getBoard();
        List<Tile> neighboringTiles = b.getNeighboringTiles(t);
        for (Tile tile : neighboringTiles){
            if (tile.getChain() != null){
                neighboringTiles.remove(tile);
            }
        }
        if (neighboringTiles.size() > 0) {
            survivingChain.addTiles(neighboringTiles);
        }
        
        //update board UI??

        //doMergeTurns

    }

    /**
     * Finds the chains touching the tile
     * @param t
     */
    private void findMergingChains(Tile t){
        HotelChain[] hotelChains = gameEngine.getHotelChains();
        mergingChains = new ArrayList<>();

        for (int i = 0; i < hotelChains.length; i++){
            //for each tile in the hotel chain, check to see if t is adjacent
            for (Tile tile : hotelChains[i].getTiles()){
                if (tile.getRowIndex() == t.getRowIndex() && tile.getColumnIndex() == t.getColumnIndex() + 1){
                    mergingChains.add(hotelChains[i]);
                } else if (tile.getRowIndex() == t.getRowIndex() && tile.getColumnIndex() == t.getColumnIndex() - 1){
                    mergingChains.add(hotelChains[i]);
                } else if (tile.getRowIndex() == t.getRowIndex() + 1 && tile.getColumnIndex() == t.getColumnIndex()){
                    mergingChains.add(hotelChains[i]);
                } else if (tile.getRowIndex() == t.getRowIndex() - 1 && tile.getColumnIndex() == t.getColumnIndex()){
                    mergingChains.add(hotelChains[i]);
                }
            }
        }
    }

    /**
     * Finds the chain that will survive the merge. If the surviving chain can't be found without the player
     * choosing, it will update the UI to show the selection for the chain.
     * @param chains
     */
    private void findSurvivingChain(ArrayList<HotelChain> chains){
        //find largest chains
        int largestSize = 0;
        for (HotelChain chain : chains){
            largestSize = Math.max(chain.size(), largestSize);
        }

        largestChains = new ArrayList<>();
        for (HotelChain chain : chains){
            if (chain.size() == largestSize){
                largestChains.add(chain);
            }
        }

        if (largestChains.size() == 1){
            survivingChain = largestChains.get(0);
        } else {
            //if more than one, update ui to have player pick
            survivingChain = null;
        }
    }

    public ArrayList<HotelChain> getLargestChains(){
        return largestChains;
    }

    public void setSurvivingChain(HotelChain chain){
        survivingChain = chain;
    }
}