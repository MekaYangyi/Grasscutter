package emu.grasscutter.server.packet.recv;

import emu.grasscutter.game.world.Position;
import emu.grasscutter.game.world.Scene;
import emu.grasscutter.net.packet.Opcodes;
import emu.grasscutter.net.packet.PacketHandler;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.HomeChangeModuleReqOuterClass;
import emu.grasscutter.server.event.player.PlayerTeleportEvent.TeleportType;
import emu.grasscutter.server.game.GameSession;
import emu.grasscutter.server.packet.send.PacketHomeAvatarTalkFinishInfoNotify;
import emu.grasscutter.server.packet.send.PacketHomeChangeModuleRsp;
import emu.grasscutter.server.packet.send.PacketHomeComfortInfoNotify;
import emu.grasscutter.server.packet.send.PacketPlayerHomeCompInfoNotify;

@Opcodes(PacketOpcodes.HomeChangeModuleReq)
public class HandlerHomeChangeModuleReq extends PacketHandler {

    @Override
    public void handle(GameSession session, byte[] header, byte[] payload) throws Exception {
        HomeChangeModuleReqOuterClass.HomeChangeModuleReq req =
                HomeChangeModuleReqOuterClass.HomeChangeModuleReq.parseFrom(payload);

        if (!session.getPlayer().getCurHomeWorld().getGuests().isEmpty()) {
            session.send(new PacketHomeChangeModuleRsp());
            return;
        }

        session.getPlayer().setCurrentRealmId(req.getTargetModuleId());
        session.send(new PacketHomeAvatarTalkFinishInfoNotify(session.getPlayer()));
        session.send(new PacketHomeChangeModuleRsp(req.getTargetModuleId()));
        session.send(new PacketPlayerHomeCompInfoNotify(session.getPlayer()));
        session.send(new PacketHomeComfortInfoNotify(session.getPlayer()));

        int realmId = 2000 + req.getTargetModuleId();

        Scene scene = session.getPlayer().getWorld().getSceneById(realmId);
        Position pos = scene.getScriptManager().getConfig().born_pos;

        session
                .getPlayer()
                .getWorld()
                .transferPlayerToScene(session.getPlayer(), realmId, TeleportType.WAYPOINT, pos);
    }
}
