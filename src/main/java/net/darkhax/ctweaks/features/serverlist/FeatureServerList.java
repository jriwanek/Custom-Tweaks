package net.darkhax.ctweaks.features.serverlist;

import net.darkhax.ctweaks.features.Feature;
import net.darkhax.ctweaks.lib.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraftforge.common.config.Configuration;

public class FeatureServerList extends Feature {

    private String[] serverEntries;
    private String[] removeEntries;
    private ServerList serverList;

    @Override
    public void onPreInit () {

        this.serverList = new ServerList(Minecraft.getMinecraft());
        this.serverList.loadServerList();

        Minecraft.getMinecraft().addScheduledTask( () -> this.removeServers());
        Minecraft.getMinecraft().addScheduledTask( () -> this.addServers());
        this.serverList.saveServerList();
    }

    @Override
    public void setupConfig (Configuration config) {

        this.serverEntries = config.getStringList("servers", this.configName, new String[] { "Example Server 1_127.0.0.1:25566", "Example Server 2_192.168.1.254" }, "Servers on this list will be automatically added to the players server list, if they do not already exist. Format is name@@serverAdress. The @ character is used to split the name from the IP adress, so it should not be used in the server name.");
        this.removeEntries = config.getStringList("removeEntries", this.configName, new String[] { "192.168.1.1", "192.168.1.2:25565", "play.olddomain.xyz" }, "Server IPs on this list will be automatically removed from the players server list. Allows for old server IPs to be removed from the server list.");
    }

    /**
     * Loops through all servers, and removes those with matching IPs to those in
     * {@link #removeEntries}.
     */
    private void removeServers () {

        for (final String removeIP : this.removeEntries) {
            for (final ServerData serverEntry : this.serverList.servers) {
                if (serverEntry.serverIP.equals(removeIP)) {
                    this.serverList.servers.remove(serverEntry);
                }
            }
        }
    }

    /**
     * Loops through all server entries and attempts to add them to the server list.
     */
    private void addServers () {

        for (final String entry : this.serverEntries) {

            final String[] parameters = entry.split("@@");

            if (parameters.length == 2) {

                boolean serverExists = false;

                for (final ServerData serverEntry : this.serverList.servers) {

                    if (serverEntry.serverName.equals(parameters[0])) {

                        if (!serverEntry.serverIP.equals(parameters[1])) {
                            serverEntry.serverIP = parameters[1];
                        }

                        serverExists = true;
                        break;
                    }
                }

                if (!serverExists) {

                    final ServerData data = new ServerData(parameters[0], parameters[1], false);
                    this.serverList.addServerData(data);
                }

                this.serverList.saveServerList();
            }
            else {
                Constants.LOG.warn(String.format("The server entry %s does not have the right amount of parameters. Expected 2, got %n", entry, parameters.length));
            }
        }
    }

}