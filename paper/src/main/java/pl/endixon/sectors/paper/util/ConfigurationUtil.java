package pl.endixon.sectors.paper.util;

public interface ConfigurationUtil {

    int BORDER_MESSAGE_DISTANCE = 15;
    int BREAK_BORDER_DISTANCE = 15;
    int PLACE_BORDER_DISTANCE = 15;
    int EXPLOSION_BORDER_DISTANCE = 15;
    int BUCKET_BORDER_DISTANCE = 15;
    String SECTOR_CONNECTED_MESSAGE = "&#ff5555Połączono się na sektor &#f5c542{SECTOR}";
    String SECTOR_ERROR_TITLE = "&#ff5555Błąd";
    String SECTOR_FULL_SUBTITLE = "&#ef4444Sektor jest pełen graczy!";
    String BORDER_MESSAGE = "&#f5c542Zbliżasz się do granicy sektora &#4ade80{SECTOR} &#7dd3fc{DISTANCE}m";
    String BREAK_BORDER_DISTANCE_MESSAGE = "&#ef4444Nie możesz niszczyć bloków przy sektorze!";
    String PLACE_BORDER_DISTANCE_MESSAGE = "&#ef4444Nie możesz stawiać bloków przy sektorze!";
    String SECTOR_DISABLED_SUBTITLE = "&#ef4444Ten sektor jest aktualnie wyłączony";
    String playerAlreadyConnectedMessage = "&#ef4444Jesteś aktualnie połączony z tym kanałem";
    String sectorIsOfflineMessage = "&#ef4444Sektor z którym chcesz się połączyć jest aktualnie wyłączony!";
    String playerDataNotFoundMessage = "&#ef4444Profil użytkownika nie został znaleziony!";
    String TITLE_WAIT_TIME = "&#ef4444Musisz odczekać {SECONDS}s przed ponowną zmianą sektora";
    String spawnSectorNotFoundMessage = "&#ef4444Nie odnaleziono dostepnego sektora spawn";
    String SectorNotFoundMessage = "&#ef4444Brak dostępnych sektorów";
}
