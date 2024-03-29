package isu.engine;

import com.sun.net.httpserver.Filter;

import javax.print.DocFlavor;
import java.util.*;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Handler;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class Bank {

    private StockSet stocks;

    public Bank(HotelChain[] chains, int stockCount){
        stocks = new StockSet(chains, stockCount);
    }

    public int getStockCount(HotelChain chain){
        return stocks.getStocks(chain);
    }

    public void payBonus(HotelChain chain, List<Player> players){

        List<Player> stockHolders = new ArrayList<Player>();

        for(Player player: players){
            if(player.getStocks(chain) != 0){
                stockHolders.add(player);
            }
        }


        if (stockHolders.size() == 1){
            //pay player both bonuses
            stockHolders.get(0).addMoney(chain.getFirstBonus() + chain.getSecondBonus());

        } else if (stockHolders.size() != 0) {
            int largest = 0;

            for(Player player: stockHolders){
                largest = Math.max(player.getStocks(chain), largest);
            }


            List<Player> majorStockHolders = new ArrayList<Player>();
            for(Player player: stockHolders){
                if(player.getStocks(chain) == largest){
                    majorStockHolders.add(player);
                }
            }

            int secondLargest = 0;

            for(Player player: stockHolders){
                int stocks = player.getStocks(chain);
                if(stocks < largest){
                    secondLargest = Math.max(stocks, secondLargest);
                }
            }



            List<Player> minorStockHolders = new ArrayList<Player>();

            for(Player player: stockHolders){
                if(player.getStocks(chain) == secondLargest){
                    minorStockHolders.add(player);
                }

            }

            if(stockHolders.size() == 2){
                if(majorStockHolders.size() != 0 && minorStockHolders.size() != 0) {
                    for (int i = 0; i < majorStockHolders.size(); i++) {
                        majorStockHolders.get(i).addMoney(chain.getFirstBonus());
                    }
                    for (int i = 0; i < minorStockHolders.size(); i++) {
                        minorStockHolders.get(i).addMoney(chain.getSecondBonus());
                    }
                }
                else if (majorStockHolders.size() == 2 && minorStockHolders.size() == 0){
                    majorStockHolders.get(0).addMoney((chain.getFirstBonus() + chain.getSecondBonus()) / 2);
                    majorStockHolders.get(1).addMoney((chain.getFirstBonus() + chain.getSecondBonus()) / 2);
                }

            }
            if(stockHolders.size() >= 3){
                // case 1: 1 major stockholder, one minor stockholder
                if (majorStockHolders.size() == 1 && minorStockHolders.size() == 1){
                    majorStockHolders.get(0).addMoney(chain.getFirstBonus());
                    minorStockHolders.get(0).addMoney(chain.getSecondBonus());
                }
                // case 2: 1 major stockholder, several minor stockholders
                else if (majorStockHolders.size() == 1 && minorStockHolders.size() >= 2){
                    majorStockHolders.get(0).addMoney(chain.getFirstBonus());
                    for (int i = 0; i< minorStockHolders.size(); i++){
                        minorStockHolders.get(i).addMoney(chain.getSecondBonus() / minorStockHolders.size());
                    }
                }
                // case 3: several major stockholders, 1 minor stockholders
                else if (majorStockHolders.size() >= 2 && minorStockHolders.size() == 1){
                    minorStockHolders.get(0).addMoney(chain.getSecondBonus());
                    for (int i = 0; i < majorStockHolders.size(); i++){
                        majorStockHolders.get(i).addMoney(chain.getFirstBonus() / majorStockHolders.size());
                    }
                }

                // case 4: several major stockholders, several minor stockholders
                else if (majorStockHolders.size() >= 2 && minorStockHolders.size() >= 2){
                    for (int i = 0; i < majorStockHolders.size(); i++){
                        majorStockHolders.get(i).addMoney(chain.getFirstBonus() / majorStockHolders.size());
                    }
                    for (int i = 0; i < minorStockHolders.size(); i++){
                        minorStockHolders.get(i).addMoney(chain.getSecondBonus() / minorStockHolders.size());
                    }
                }
                // case 5: 0 minor stockholders(everyone is major stockholders)
                else if (minorStockHolders.size() == 0){
                    for (int i = 0; i < majorStockHolders.size(); i++){
                        majorStockHolders.get(i).addMoney((chain.getFirstBonus() + chain.getSecondBonus()) / majorStockHolders.size());
                    }
                }
            }
        }
    }

    public void sellStocksToPlayer(Player player, HotelChain chain, int numStocks){
        if(numStocks == 0) return;

        stocks.removeStocks(chain, numStocks);
        player.addStocks(chain, numStocks);

        player.pullMoney(PriceChart.getStockPrice(chain.getCategory(), chain.size()) * numStocks);

    }

    public void buyStocksFromPlayer(Player player, HotelChain chain, int numStocks){
        if(numStocks == 0) return;

        player.removeStocks(chain, numStocks);
        stocks.removeStocks(chain, numStocks);
        stocks.addStocks(chain, numStocks);

        player.addMoney(PriceChart.getStockPrice(chain.getCategory(), chain.size()) * numStocks);
    }

    public void tradeStocksWithPlayer(Player player, HotelChain majorChain,
                                      HotelChain minorChain, int minorStockCount){

        if(minorStockCount == 0) return;


    }

    public void giveFreeStockToPlayer(Player player, HotelChain chain){
        if(stocks.getStocks(chain) == 0)return;
        stocks.removeStocks(chain, 1);
        player.addStocks(chain, 1);
    }
}