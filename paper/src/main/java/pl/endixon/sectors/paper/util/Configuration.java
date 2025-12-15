/*
 *
 *  EndSectors  Non-Commercial License
 *  (c) 2025 Endixon
 *
 *  Permission is granted to use, copy, and
 *  modify this software **only** for personal
 *  or educational purposes.
 *
 *  Commercial use, redistribution, claiming
 *  this work as your own, or copying code
 *  without explicit permission is strictly
 *  prohibited.
 *
 *  Visit https://github.com/Endixon/EndSectors
 *  for more info.
 *
 */

package pl.endixon.sectors.paper.util;

import pl.endixon.sectors.common.util.ChatUtil;

public interface Configuration {

    String BORDER_MESSAGE = ChatUtil.fixHexColors(
            "&#f5c542Zbliżasz się do granicy sektora &#4ade80{SECTOR} &#7dd3fc{DISTANCE}m"
    );
    int BORDER_MESSAGE_DISTANCE = 15;

    int BREAK_BORDER_DISTANCE = 15;
    String BREAK_BORDER_DISTANCE_MESSAGE = ChatUtil.fixHexColors(
            "&#ef4444Nie możesz niszczyć bloków przy sektorze!"
    );

    int PLACE_BORDER_DISTANCE = 15;
    String PLACE_BORDER_DISTANCE_MESSAGE = ChatUtil.fixHexColors(
            "&#ef4444Nie możesz stawiać bloków przy sektorze!"
    );

    String SECTOR_DISABLED_TITLE = ChatUtil.fixHexColors(
            "&#ef4444Błąd"
    );
    String SECTOR_DISABLED_SUBTITLE = ChatUtil.fixHexColors(
            "&#9ca3afTen sektor jest aktualnie &#f87171wyłączony"
    );

    int EXPLOSION_BORDER_DISTANCE = 15;
    int BUCKET_BORDER_DISTANCE = 15;


    String playerAlreadyConnectedMessage = ChatUtil.fixHexColors(
            "&#f87171Jesteś aktualnie połączony z tym kanałem"
    );

    String sectorIsOfflineMessage = ChatUtil.fixHexColors(
            "&#fb7185Sektor z którym chcesz się połączyć jest aktualnie wyłączony!"
    );

    String playerDataNotFoundMessage = ChatUtil.fixHexColors(
            "&#f59e0bWystąpił problem podczas ładowania danych"
    );

    double BORDER_TELEPORT_DISTANCE = 10;
    String BORDER_TELEPORT_DISTANCE_MESSAGE = ChatUtil.fixHexColors(
            "&#ef4444Nie możesz użyć ender perla tak blisko granicy sektora!"
    );

}
