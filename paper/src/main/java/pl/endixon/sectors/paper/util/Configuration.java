/*
 * 
 *  EndSectors  Non-Commercial License         
 *  (c) 2025 Endixon                             
 *                                              
 *  Permission is granted to use, copy, and    
 *  modify this software **only** for personal 
 *  or educational purposes.                   
 *                                              
 *   Commercial use, redistribution, claiming
 *  this work as your own, or copying code     
 *  without explicit permission is strictly    
 *  prohibited.                                
 *                                              
 *  Visit https://github.com/Endixon/EndSectors
 *  for more info.                             
 * 
 */


package pl.endixon.sectors.paper.util;

public interface Configuration {


    String BORDER_MESSAGE = "&6Zbliżasz się do granicy sektora &a{SECTOR} {DISTANCE}m";
    int BORDER_MESSAGE_DISTANCE = 15;
    int BREAK_BORDER_DISTANCE = 10;
    String BREAK_BORDER_DISTANCE_MESSAGE = "&cNie możesz niszczyć bloków przy sektorze!";
    int PLACE_BORDER_DISTANCE = 10;
    String PLACE_BORDER_DISTANCE_MESSAGE = "&cNie możesz stawiać bloków przy sektorze!";
    String SECTOR_DISABLED_TITLE = "&cBłąd";
    String SECTOR_DISABLED_SUBTITLE = "&7Ten sektor jest aktualnie &cwyłączony";
    int EXPLOSION_BORDER_DISTANCE = 10;
    int BUCKET_BORDER_DISTANCE = 10;
    String playerAlreadyConnectedMessage = "&cJesteś aktualnie połączony z tym kanałem";
    String sectorIsOfflineMessage = "&cSektor z którym chcesz się połączyć jest aktualnie wyłączony!";
    String playerDataNotFoundMessage = "&cWystąpił problem podczas ładowania danych";
    double BORDER_TELEPORT_DISTANCE = 10;
    String BORDER_TELEPORT_DISTANCE_MESSAGE = "&cNie możesz użyć ender perla tak blisko granicy sektora!";


}

