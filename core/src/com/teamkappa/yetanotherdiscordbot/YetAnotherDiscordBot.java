package com.teamkappa.yetanotherdiscordbot;

import net.dv8tion.jda.JDA;
import net.dv8tion.jda.JDABuilder;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.events.message.MessageReceivedEvent;
import net.dv8tion.jda.hooks.ListenerAdapter;
import net.dv8tion.jda.player.MusicPlayer;
import net.dv8tion.jda.player.hooks.events.NextEvent;
import net.dv8tion.jda.player.hooks.events.PlayEvent;

import javax.security.auth.login.LoginException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Sas Luca on 01-Jul-16.
 * Copyright (c) Team Kappa.
 */

public class YetAnotherDiscordBot extends ListenerAdapter
{
    private static boolean _Connected;
    private static MusicPlayer _MusicPlayer;
    private static TextChannel _PlayerTextChannel;
    private static MessageReceivedEvent _MessageReceivedEvent;

    public static final String TRIGGER = "†";
    public static final HashMap<String, ICommand> COMMANDS = new HashMap<>();
    public static final YetAnotherDiscordBot INSTANCE = new YetAnotherDiscordBot();

    public enum ConnectionAttemptState { SUCCESSFUL, WRONG_CREDENTIALS, ERROR }
    public static ConnectionAttemptState connect(String token)
    {
        try
        {
            JDA jda = new JDABuilder().setBotToken(token).addListener(INSTANCE).buildBlocking();
            jda.getAccountManager().setGame("PM " + TRIGGER + "commands");
        }
        catch (LoginException e) { System.out.println("The provided email / password combination was incorrect. Please provide valid details."); return ConnectionAttemptState.WRONG_CREDENTIALS; }
        catch (InterruptedException e) { e.printStackTrace(); return ConnectionAttemptState.ERROR; }

        _Connected = true;

        return ConnectionAttemptState.SUCCESSFUL;
    }

    //<editor-fold desc="Listener">
    private YetAnotherDiscordBot()
    {
        _MusicPlayer = new MusicPlayer();
        _MusicPlayer.addEventListener((playerEvent) ->
        {
            if (playerEvent instanceof PlayEvent) _PlayerTextChannel.sendMessage("**Playing:** ***" + _MusicPlayer.getCurrentAudioSource().getInfo().getTitle() + "*** - __**[" + _MusicPlayer.getCurrentAudioSource().getInfo().getDuration().getHours() + ":" + _MusicPlayer.getCurrentAudioSource().getInfo().getDuration().getMinutes() + ":" + _MusicPlayer.getCurrentAudioSource().getInfo().getDuration().getSeconds() + "]**__");
            if (playerEvent instanceof NextEvent) _PlayerTextChannel.sendMessage("**Playing:** ***" + _MusicPlayer.getCurrentAudioSource().getInfo().getTitle() + "*** - __**[" + _MusicPlayer.getCurrentAudioSource().getInfo().getDuration().getHours() + ":" + _MusicPlayer.getCurrentAudioSource().getInfo().getDuration().getMinutes() + ":" + _MusicPlayer.getCurrentAudioSource().getInfo().getDuration().getSeconds() + "]**__");
        });
    }

    @Override public void onMessageReceived(MessageReceivedEvent event)
    {
        _MessageReceivedEvent = event;

        if(event.isPrivate())
        {
            //<editor-fold desc="Private shit that does not matter right now">
            /*String pcommand;
            String pallArguments = "";
            String[] parguments = new String[0];

            String prawMessage = event.getMessage().getRawContent();
            String pparts[] = prawMessage.split(" ", 2);
            pcommand = pparts[0];

            if (pparts.length == 2)
            {
                pallArguments = pparts[1];
                parguments = pallArguments.split(" ");
            }
            if (pcommand.equalsIgnoreCase(TRIGGER + "commands"))
            {
                String a = "```Commands 1/2: \n";

                for(String command : m_Definitions.keySet()) a = a + "\n" + command + " -> " + m_Definitions.get(command);

                a = a + "```";
                pPrint(a);

                String b = "```Commands 2/2: \n";

                for(String command : m_Definitions2.keySet())
                {
                    b = b + "\n" + command + " -> " + m_Definitions2.get(command);
                }
                b = b + "```";
                pPrint(b);

                System.out.println("User: @" + event.getAuthor().getUsername() + " used command: \"commands\" on private");
            }
            else if (pcommand.equalsIgnoreCase(m_Trigger + "join"))
            {
                pPrint("The invite link is:\nhttps://discordapp.com/oauth2/authorize?&client_id=171966919103086592&scope=bot");
            } */
            //</editor-fold>
        }

        if (!event.isPrivate())
        {
            String args = null;
            String[] parts = event.getMessage().getRawContent().split(" ", 2);

            if (parts.length == 2) args = parts[1];

            //parts[0] is the command
            COMMANDS.get(parts[0].toLowerCase()).command(args);
        }
    }

    @Override public void onGuildMemberJoin(GuildMemberJoinEvent m_Join)
    {
        //m_Join.getGuild().getPublicChannel().sendMessage("***" + m_Join.getUser().getUsername() + "***" + "** has joined the server**");
        //defmsg("__***" + m_Join.getUser().getUsername() + "***__" + " **has joined the server**");
        //m_Join.getGuild().getPublicChannel().sendMessage("**" + m_Join.getUser().getUsername() + "** ***has joined the server***");

        /*
        try{
            DataInputStream dis = new DataInputStream (new FileInputStream ("defrole.txt"));

            byte[] datainBytes = new byte[dis.available()];
            dis.readFully(datainBytes);
            dis.close();

            String cnt = new String(datainBytes, 0, datainBytes.length);

            int i = 0;
            List<String> role = new ArrayList<String>();
            for (int j = 0; j < m_Join.getGuild().getRoles().size(); j++)
            {
                System.out.println(m_Join.getGuild().getRoles().get(j).getName());
                if (m_Join.getGuild().getRoles().get(j).getName().matches(cnt))
                {
                    i = j;
                }
            }
            m_Join.getGuild().getManager().addRoleToUser(m_Join.getUser(), m_Join.getGuild().getRoles().get(i)).update();

        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }*/

        // Artzy custom role
        if (m_Join.getGuild().getId().matches("159066954424909824"))
        {
            int i = 0;
            List<String> role = new ArrayList<String>();
            for (int j = 0; j < m_Join.getGuild().getRoles().size(); j++)
            {
                System.out.println(m_Join.getGuild().getRoles().get(j).getName());
                if (m_Join.getGuild().getRoles().get(j).getName().matches("Shugar Cubes"))
                {
                    i = j;
                }
            }
            m_Join.getGuild().getManager().addRoleToUser(m_Join.getUser(), m_Join.getGuild().getRoles().get(i)).update();
        }
    }
    //</editor-fold>

    public static boolean isConnected() { return _Connected; }
    public static MusicPlayer getMusicPlayer() { return _MusicPlayer; }
    public static TextChannel getTextChannel() { return _PlayerTextChannel; }
    public static MessageReceivedEvent getMessageReceivedEvent() { return _MessageReceivedEvent; }
}
