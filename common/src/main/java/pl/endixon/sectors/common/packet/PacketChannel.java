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

package pl.endixon.sectors.common.packet;

public interface PacketChannel {

    String PACKET_CONFIGURATION_REQUEST = "PacketConfigurationRequest";
    String PACKET_TELEPORT_HOME = "PacketTeleportHome";
    String PACKET_BROADCAST_MESSAGE = "PacketBroadcastMessage";
    String PACKET_SEND_MESSAGE_TO_PLAYER = "PacketSendMessageToPlayer";
    String PACKET_BROADCAST_TITLE = "PacketBroadcastTitle";
    String USER_CHECK_REQUEST = "USER_CHECK_REQUEST";
    String USER_CHECK_RESPONSE = "USER_CHECK_RESPONSE";
    String PACKET_TELEPORT_TO_SECTOR = "TeleportToSector";
    String PACKET_SECTOR_CONNECTED = "PacketSectorConnected";
    String PACKET_SECTOR_DISCONNECTED = "PacketSectorDisconnected";
    String PACKET_SECTOR_INFO_QUEUE = "PacketSectorInfoQueue";
    String PACKET_EXECUTE_COMMAND = "PacketExecuteCommand";
    String PACKET_PLAYER_INFO_REQUEST = "PacketPlayerInfoRequest";
    String PACKET_SECTOR_CHAT_BROADCAST = "PacketSectorChatBroadcast";
    String PACKET_SECTOR_INFO = "PacketSectorInfo";
}
