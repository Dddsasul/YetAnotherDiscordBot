package com.teamkappa.yetanotherdiscordbot;

import net.dv8tion.jda.JDA;
import net.dv8tion.jda.JDABuilder;
import net.dv8tion.jda.audio.player.URLPlayer;
import net.dv8tion.jda.entities.*;
import net.dv8tion.jda.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.events.message.MessageReceivedEvent;
import net.dv8tion.jda.hooks.ListenerAdapter;
import net.dv8tion.jda.player.MusicPlayer;
import net.dv8tion.jda.player.Playlist;
import net.dv8tion.jda.player.hooks.PlayerEventListener;
import net.dv8tion.jda.player.hooks.events.NextEvent;
import net.dv8tion.jda.player.hooks.events.PlayEvent;
import net.dv8tion.jda.player.hooks.events.PlayerEvent;
import net.dv8tion.jda.player.source.AudioSource;
import net.dv8tion.jda.player.source.RemoteSource;
import net.dv8tion.jda.utils.AvatarUtil;
import org.json.JSONException;
import org.json.JSONObject;

import javax.security.auth.login.LoginException;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class DeprecatedDiscordBot extends ListenerAdapter
{
    private MessageReceivedEvent m_Event;
    private GuildMemberJoinEvent m_Join;
    private static DeprecatedDiscordBot m_Bot;
    private String m_Trigger = "†";
    private String m_AllArguments;
    private String[] m_Arguments;
    private boolean m_Connected;
    private Map<String, String> m_Definitions;
    private Map<String, String> m_Definitions2;
    private String[] parts;
    private int bullet = 1;
    private boolean joinRole = true;
    public URLPlayer up = null;
    public MusicPlayer player = new MusicPlayer();
    private List<String> urlqueue = new ArrayList<>();
    private TextChannel playT;
    private int score = 0;

    public List<String> YOUTUBE_DL_LAUNCH_ARGS = Collections.unmodifiableList(Arrays.asList(
            "python",               //Launch python executor
            "./youtube-dl",         //youtube-dl program file
            "-q",                   //quiet. No standard out.
            "-f", "bestaudio/best", //Format to download. Attempts best audio-only, followed by best video/audio combo
            "--no-playlist",        //If the provided link is part of a Playlist, only grabs the video, not playlist too.
            "-o", "-"               //Output, output to STDout
    ));

    public List<String> FFMPEG_LAUNCH_ARGS =
            Collections.unmodifiableList(Arrays.asList(
                    "ffmpeg",       //Program launch
                    "-i", "-",      //Input file, specifies to read from STDin (pipe)
                    "-f", "s16be",  //Format.  PCM, signed, 16bit, Big Endian
                    "-ac", "2",     //Channels. Specify 2 for stereo audio.
                    "-ar", "48000", //Rate. Opus requires an audio rate of 48000hz
                    "-map", "a",    //Makes sure to only output audio, even if the specified format supports other streams
                    "-"             //Used to specify STDout as the output location (pipe)
            ));

    private DeprecatedDiscordBot()
    {
        m_Definitions = new LinkedHashMap<String, String>();
        m_Definitions2 = new LinkedHashMap<String, String>();

        m_Definitions.put(m_Trigger + "join", "Use this command in PM to ge the invite link for the bot");
        m_Definitions.put(m_Trigger + "info", "Take a wild guess at what this command does?");
        m_Definitions.put(m_Trigger + "ping", "pong");
        m_Definitions.put(m_Trigger + "pong", "Dennied");
        m_Definitions.put(m_Trigger + "say", "Do I really need to spell to you what this command does?");
        m_Definitions.put(m_Trigger + "avatar", "Displays the avatar of all mentioned users");
        m_Definitions.put(m_Trigger + "doge", "Displays 1/10 random doge images");
        m_Definitions.put(m_Trigger + "facepalm", "The bot facepalms at the stupidity around him");
        m_Definitions.put(m_Trigger + "ping", "pong");
        m_Definitions.put(m_Trigger + "roll", "Rolls the dice");
        m_Definitions.put(m_Trigger + "dice", "Same ^^");
        m_Definitions.put(m_Trigger + "choose", "The bot will choose for you, each choice must start with ~ ex: '"+ m_Trigger + "choose ~option 1 ~option 2'");
        m_Definitions.put(m_Trigger + "stream", "Usage: '" + m_Trigger + "stream (link)' it works with soundcloud, and youtube!");
        m_Definitions.put(m_Trigger + "stop", "Stops the music and disconects the bot from the voice channel, also clears the playlist");
        m_Definitions.put(m_Trigger + "cat", "Displays a random cat image");
        m_Definitions.put(m_Trigger + "penguin", "Displays a random penguin image");
        m_Definitions.put(m_Trigger + "8ball", "Answers a yes/no type question");
        m_Definitions.put(m_Trigger + "8doge", "Answers a yes/no type question in doge style, much awesome");
        m_Definitions.put(m_Trigger + "notbad", "Not bad Obama meme");
        m_Definitions.put(m_Trigger + "godwhy", "God Why meme");
        m_Definitions.put(m_Trigger + "roulette", "Usage: '" + m_Trigger + "roulette' spins the revolver barrel every time; '" + m_Trigger + "roulette next' does not spin the revolver barrel, if you did not die 5 times 6th one is guaranteed death");
        m_Definitions.put(m_Trigger + "baka", "Usage: '" + m_Trigger + "baka @user(s)' result = @user(s), \uD83D\uDCA2 YOU BAKA!");
        m_Definitions.put(m_Trigger + "rps", "Usage: '" + m_Trigger + "rps rock/paper/scissors' and the result should be obvious");
        m_Definitions.put(m_Trigger + "rip", "Usage: '" + m_Trigger + "rip (insert whatever here)' or just lleave it blank");
        m_Definitions.put(m_Trigger + "svinfo", "Displays some server related information");
        m_Definitions.put(m_Trigger + "serverid", "Displays the server id... (Tahnks captain obvious)");
        m_Definitions.put(m_Trigger + "svid", "Same ^^");
        m_Definitions.put(m_Trigger + "stats", "Displays some basic stats for the bot");
        m_Definitions.put("..trigger", "pastes the trigger symbole (mostly for phone users)");
        m_Definitions.put(m_Trigger + "isonline", "Usage: '" + m_Trigger + "isonline (twitch_name)' to check if that person is streaming or not.");
        m_Definitions.put(m_Trigger + "setdsfchan", "Sets the default channel for announcing the custom join, leave and ban messages");
        m_Definitions.put(m_Trigger + "setdsfchan", "Sets the default role for new users");
        m_Definitions.put(m_Trigger + "dennied", "just try it =]");
        m_Definitions.put(m_Trigger + "myid", "It will tell you your Discord id");
        m_Definitions2.put(m_Trigger + "fuckyou", "Requires 'Rude' role for the bot to reply");
        m_Definitions2.put(m_Trigger + "silentid", "Displays the ID and Discriminator of the mentioned user withoth mentioning them (you use just the name)");
        m_Definitions2.put(m_Trigger + "sid", "Same ^^");
        m_Definitions2.put(m_Trigger + "silentavatar", "Displays the avatar of the mentioned user withoth mentioning them (you use just the ID)");
        m_Definitions2.put(m_Trigger + "savatar", "Same ^^");
    }

    /*
    TODO:

    Fix audio
    Fix duel
    Console
    @ Log command files with save option
    More random commands
    @ Twitch live announcer
    Custom commands
    Catapana easter egg
    Make more funny replies
    ignore user/channel/server
    */


    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent m_Join)
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

    /*@Override
    public void onGuildMemberBan(GuildMemberBanEvent m_Ban)
    {
        m_Ban.getGuild().getPublicChannel().sendMessage("**" + m_Ban.getUser().getUsername() + "**" + " ***has been banned!***");
    }*/

    /*@Override
    public void onGuildMemberUnban(GuildMemberUnbanEvent m_Unban)
    {
        m_Unban.getGuild().getPublicChannel().sendMessage("**" + m_Unban.getUser().getUsername() + "**" + " ***has been unbanned!***");
    }*/

    /*@Override
    public void onGuildMemberLeave(GuildMemberLeaveEvent m_leave)
    {
        m_leave.getGuild().getPublicChannel().sendMessage("**" + m_leave.getUser().getUsername() + "** ***has left the server***");
    }*/


    public void onNext(NextEvent next)
    {

    }


    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
        m_Event = event;

        if(m_Event.isPrivate())
        {
            //<editor-fold desc="Private shit that does not matter right now">
            String pcommand;
            String pallArguments = "";
            String[] parguments = new String[0];

            String prawMessage = m_Event.getMessage().getRawContent();
            String pparts[] = prawMessage.split(" ", 2);
            pcommand = pparts[0];

            if (pparts.length == 2)
            {
                pallArguments = pparts[1];
                parguments = pallArguments.split(" ");
            }
            if (pcommand.equalsIgnoreCase(m_Trigger + "commands"))
            {
                String a = "```Commands 1/2: \n";

                for(String command : m_Definitions.keySet())
                {
                    a = a + "\n" + command + " -> " + m_Definitions.get(command);
                }
                a = a + "```";
                pPrint(a);

                String b = "```Commands 2/2: \n";

                for(String command : m_Definitions2.keySet())
                {
                    b = b + "\n" + command + " -> " + m_Definitions2.get(command);
                }
                b = b + "```";
                pPrint(b);

                System.out.println("User: @" + m_Event.getAuthor().getUsername() + " used command: \"commands\" on private");
            }
            else if (pcommand.equalsIgnoreCase(m_Trigger + "join"))
            {
                pPrint("The invite link is:\nhttps://discordapp.com/oauth2/authorize?&client_id=171966919103086592&scope=bot");
            }
            //</editor-fold>
        }
        if (!m_Event.isPrivate())
        {
            //<editor-fold desc="Public commands duh ">
            String parts[] = m_Event.getMessage().getRawContent().split(" ", 2);
            String command = parts[0];

            m_AllArguments = null;

            if(parts.length == 2)
            {
                m_AllArguments = parts[1];
                m_Arguments = m_AllArguments.split(" ");
            }

            if (command.equalsIgnoreCase(m_Trigger + "ping")) ping();
            else if(command.equalsIgnoreCase(m_Trigger + "say")) say();
            else if (command.equalsIgnoreCase(m_Trigger + "pong")) pong();
            else if (command.equalsIgnoreCase(m_Trigger + "info")) info();
            else if (command.equalsIgnoreCase(m_Trigger + "avatar")) avatar();
            else if (command.equalsIgnoreCase(m_Trigger + "doge")) doge();
            else if (command.equalsIgnoreCase(m_Trigger + "facepalm")) facepalm();
            else if (command.equalsIgnoreCase(m_Trigger + "svinfo")) svinfo();
            else if (command.equalsIgnoreCase(m_Trigger + "roll") || command.equalsIgnoreCase(m_Trigger + "dice")) dice();
            else if (command.equalsIgnoreCase(m_Trigger + "choose"))
            {
                choose();
            }
            else if (command.equalsIgnoreCase(m_Trigger + "stream"))
            {
                stream();
            }
            else if (command.equalsIgnoreCase(m_Trigger + "stop"))
            {
                stop();
            }
            else if (command.equalsIgnoreCase(m_Trigger + "cat"))
            {
                cat();
            }
            else if (command.equalsIgnoreCase(m_Trigger + "8doge"))
            {
                doge8();
            }
            else if (command.equalsIgnoreCase(m_Trigger + "8ball"))
            {
                ball8();
            }
            else if (command.equalsIgnoreCase(m_Trigger + "penguin"))
            {
                penguin();
            }
            else if (command.equalsIgnoreCase(m_Trigger + "notbad"))
            {
                notbad();
            }
            else if (command.equalsIgnoreCase(m_Trigger + "godwhy"))
            {
                godwhy();
            }
            else if (command.equalsIgnoreCase(m_Trigger + "roulette"))
            {
                roulette();
            }
            else if (command.equalsIgnoreCase(m_Trigger + "baka"))
            {
                baka();
            }
            else if (command.equalsIgnoreCase(m_Trigger + "rps"))
            {
                rps();
            }
            else if (command.equalsIgnoreCase(m_Trigger + "stats"))
            {
                stats();
            }
            else if (command.equalsIgnoreCase(m_Trigger + "setDefChan"))
            {
                setDefChan();
            }
            else if (command.equalsIgnoreCase(m_Trigger + "dennied"))
            {
                print("http://i.imgur.com/IpwNHDF.png");
            }
            else if (command.equalsIgnoreCase("..trigger"))
            {
                print(m_Trigger);
            }
            else if (command.equalsIgnoreCase(m_Trigger + "isonline"))
            {
                isonline();
            }
            else if (command.equalsIgnoreCase(m_Trigger + "rip"))
            {
                rip();
            }
            else if (command.equalsIgnoreCase(m_Trigger + "luca"))
            {
                luca();
            }
            else if (command.equals(m_Trigger + "serverid") || command.equalsIgnoreCase(m_Trigger + "svid"))
            {
                print("The server ID is: " + m_Event.getGuild().getId());
            }
            else if (command.equalsIgnoreCase(m_Trigger + "setdefrole"))
            {
                setDefRole();
            }
            else if (command.equalsIgnoreCase(m_Trigger + "myid"))
            {
                print("Your ID is: `" + m_Event.getAuthor().getId() + "`");
            }
            else if (command.equalsIgnoreCase(m_Trigger + "fuckyou"))
            {
                fuckyou();
            }
            else if (command.equalsIgnoreCase(m_Trigger + "silentid") || command.equalsIgnoreCase(m_Trigger + "sid"))
            {
                silentid();
            }
            else if (command.equalsIgnoreCase(m_Trigger + "silentavatar") || command.equalsIgnoreCase(m_Trigger + "savatar"))
            {
                silentavatar();
            }
            else if (command.equalsIgnoreCase(m_Trigger + "playlist"))
            {
                playlist();
            }
            else if (command.equalsIgnoreCase(m_Trigger + "skip"))
            {
                skip();
            }
            else if (command.equalsIgnoreCase(m_Trigger + "shuffle"))
            {
                shuffle();
            }
            else if (command.equalsIgnoreCase(m_Trigger + "queue"))
            {
                queue();
            }
            else if (command.equalsIgnoreCase(m_Trigger + "button"))
            {
                print("https://media.giphy.com/media/3o7qEcBToIc2S3oW7S/giphy.gif");
            }
            else if (command.equalsIgnoreCase(m_Trigger + "veganhunt"))
            {
                veganhunt();
            }
            else if (command.equalsIgnoreCase(m_Trigger + "blood"))
            {
                print("*" + m_Event.getAuthor().getUsername() + " offers blood to the mighty woolf to calm him down*");
            }
            //Test
            else if (command.equalsIgnoreCase(m_Trigger + "test"))
            {
                test();
            }
            else if (command.endsWith(m_Trigger + "skip"))
            {
                skip();
            }
            else if (command.equalsIgnoreCase(m_Trigger + "test2"))
            {
                test2();
            }
            else if (command.equalsIgnoreCase(m_Trigger + "test3"))
            {
                test3();
            }
            else if (command.equalsIgnoreCase(m_Trigger + "test4"))
            {
                test4();
            }
            else if (command.equalsIgnoreCase(m_Trigger + "test5"))
            {
                //test5();
            }
            else if (command.equalsIgnoreCase(m_Trigger + "test6"))
            {
                test6();
            }
            else if (command.equalsIgnoreCase("just"))
            {
                if (m_Event.getTextChannel().getId().matches("159066954424909824")) {print("DO IT");}
            }
            //</editor-fold>
        }
    }


    private void ping()
    {
        print("pong");
        System.out.println("User: @" + m_Event.getAuthor().getUsername() + " used command: \"ping\" on server: '" + m_Event.getGuild().getName() + "' in channel: #" + m_Event.getTextChannel().getName());
    }

    private void say()
    {
        if(m_AllArguments.contains(m_Trigger)) {
            print("Using '" + m_Trigger + "' is banned inside a say command");
        }
        else if(m_AllArguments.contains("@everyone")) {
            print("Mentioning everyone is banned inside a say command");
        }
        else {
            print(m_AllArguments);
        }
        System.out.println("User: @" + m_Event.getAuthor().getUsername() + " used command: \"say\" on server: '" + m_Event.getGuild().getName() + "' in channel: #" + m_Event.getTextChannel().getName());
    }

    private void pong()
    {
        print("Nice try!");
        System.out.println("User: @" + m_Event.getAuthor().getUsername() + " used command: \"pong\" on server: '" + m_Event.getGuild().getName() + "' in channel: #" + m_Event.getTextChannel().getName());
    }

    private void info()
    {
        print("```I am a chat bot developed by TeamKappa (Ddddsasul & Friends); PM me " + m_Trigger + "commands for a full list of commands, when completed I will be available for everyone to download me and host your own version.```");
        System.out.println("User: @" + m_Event.getAuthor().getUsername() + " used command: \"info\" on server: '" + m_Event.getGuild().getName() + "' in channel: #" + m_Event.getTextChannel().getName());
    }

    private void avatar()
    {
        List<String> usr = new ArrayList<String>();
        for (User u : m_Event.getMessage().getMentionedUsers() ) { usr.add(u.getAvatarUrl()); }
        if(!usr.isEmpty()) print(usr.toString().replace("[", "").replace("]", "").replace(",", ""));
        System.out.println("User: @" + m_Event.getAuthor().getUsername() + " used command: \"avatar\" on server: '" + m_Event.getGuild().getName() + "' in channel: #" + m_Event.getTextChannel().getName());
    }

    private void doge()
    {
        switch(getRange(1, 10))
        {
            //<editor-fold desc="Doge nude pics">
            case 1:
                print("https://pbs.twimg.com/profile_images/378800000822867536/3f5a00acf72df93528b6bb7cd0a4fd0c.jpeg");
                break;
            case 2:
                print("https://cdn.thinglink.me/api/image/727110550026190849/1240/10/scaletowidth");
                break;
            case 3:
                print("http://img06.deviantart.net/6d9c/i/2014/136/0/b/twinkie_doge_by_rayleeman-d7inngz.jpg");
                break;
            case 4:
                print("https://s-media-cache-ak0.pinimg.com/originals/d4/a9/91/d4a991c285628fa82e11355f353f6419.gif");
                break;
            case 5:
                print("https://s-media-cache-ak0.pinimg.com/564x/b9/71/0f/b9710fdfde64c6688492dc87be797da2.jpg");
                break;
            case 6:
                print("http://i.imgur.com/3RUeIox.png");
                break;
            case 7:
                print("https://pbs.twimg.com/media/Bd10tZmIEAAXS8E.jpg");
                break;
            case 8:
                print("https://i.ytimg.com/vi/ina0nStsKZI/maxresdefault.jpg");
                break;
            case 9:
                print("http://orig02.deviantart.net/a5ed/f/2014/108/5/0/muffin_doge_by_rayleeman-d7f21h0.jpg");
                break;
            case 10:
                print("http://i.imgur.com/VnPAdH3.jpg");
                break;
            //</editor-fold>
        }

        System.out.println("User: @" + m_Event.getAuthor().getUsername() + " used command: \"doge\" on server: '" + m_Event.getGuild().getName() + "' in channel: #" + m_Event.getTextChannel().getName());

    }

    private void facepalm()
    {
        print("https://giphy.com/gifs/to-aru-kagaku-no-railgun-JRMvrNMKfjqmI");

        System.out.println("User: @" + m_Event.getAuthor().getUsername() + " used command: \"facepalm\" on server: '" + m_Event.getGuild().getName() + "' in channel: #" + m_Event.getTextChannel().getName());
    }

    private void svinfo()
    {
        List<String> rls = new ArrayList<String>();
        for ( Role s : m_Event.getGuild().getRoles() ){ rls.add(s.getName()); }

        List<String> tchn = new ArrayList<String>();
        for ( TextChannel t : m_Event.getGuild().getTextChannels() ) { tchn.add(t.getName()); }

        List<String> vchn = new ArrayList<String>();
        for ( VoiceChannel v : m_Event.getGuild().getVoiceChannels() ) { vchn.add(v.getName()); }

        String roles = rls.toString().substring(1, rls.toString().length()- 1);

        Guild sv = m_Event.getGuild();
        print("```ruby\nName: " + sv.getName() + "\nID: " + sv.getId() + "\nLocation: " + sv.getRegion() + "\nCreator: " + sv.getJDA().getUserById(sv.getOwnerId()).getUsername() + " ID: " + sv.getOwnerId() + "\nText Channels: " + tchn.toString().substring(1, tchn.toString().length()- 1) + "\nVoice Channels: " + vchn.toString().substring(1, vchn.toString().length()- 1) + "\nRoles: " + roles.replace(", @everyone", "")  + "\nAvatar: " + sv.getIconUrl() + "```");
        System.out.println("User: @" + m_Event.getAuthor().getUsername() + " used command: \"svinfo\" on server: '" + m_Event.getGuild().getName() + "' in channel: #" + m_Event.getTextChannel().getName());
    }

    private void dice()
    {
        int rl = 100;

        if(m_Event.getMessage().getRawContent().split(" ").length > 2) return;
        else if(m_Event.getMessage().getRawContent().split(" ").length == 0)
            rl = isInteger(m_Event.getMessage().getRawContent().split(" ")[0]) ? Integer.parseInt(m_Event.getMessage().getRawContent().split(" ")[0]) : rl;

        int r = getRange(1, rl);
        print("You rolled: " + r + "/" + rl);
        System.out.println("User: @" + m_Event.getAuthor().getUsername() + " used command: \"roll\" on server: '" + m_Event.getGuild().getName() + "' in channel: #" + m_Event.getTextChannel().getName());
    }

    private void choose()
    {
        String commandString = m_Event.getMessage().getContent();
        String[] cha = commandString.split("~");
        int n = getRange(1, cha.length - 1);
        print(cha[n]);
        m_Event.getJDA().getAudioManager(m_Event.getGuild()).closeAudioConnection();
        System.out.println("User: @" + m_Event.getAuthor().getUsername() + " used command: \"choose\" on server: '" + m_Event.getGuild().getName() + "' in channel: #" + m_Event.getTextChannel().getName());
    }

    private void cat()
    {
        URL url = null;
        try {
            url = new URL("http://random.cat/meow");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        URLConnection con = null;
        try {
            con = url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        InputStream in = null;
        try {
            in = con.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String encoding = con.getContentEncoding();
        encoding = encoding == null ? "UTF-8" : encoding;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int len = 0;
        try {
            while ((len = in.read(buf)) != -1) {
                baos.write(buf, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        String body = null;
        try {
            body = new String(baos.toByteArray(), encoding);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String bd = body.replace("{\"file\":\"http:\\/\\/random.cat\\/i\\/", "").replace("\"}", "");
        print("http://random.cat/i/" + bd);
    }

    private void doge8()
    {
        String spart = null;
        switch (getRange(1, 11))
        {
            case 1:
                spart = "Much";
                break;
            case 2:
                spart = "Many";
                break;
            case 3:
                spart = "Few";
                break;
            case 4:
                spart = "Little";
                break;
            case 5:
                spart = "Very";
                break;
            case 6:
                spart = "Not";
                break;
            case 7:
                spart = "Yes";
                break;
            case 8:
                spart = "Wow";
                break;
            case 9:
                spart = "Amaze";
                break;
            case 10:
                spart = "So";
                break;
            case 11:
                spart = "Excite";
                break;
        }

        String npart = null;
        switch (getRange(1, 9))
        {
            case 1:
                npart = " yes";
                break;
            case 2:
                npart = " no";
                break;
            case 3:
                npart = " not";
                break;
            case 4:
                npart = " never";
                break;
            case 5:
                npart = " probably";
                break;
            case 6:
                npart = " doubtful";
                break;
            case 7:
                npart = " likely";
                break;
            case 8:
                npart = " unknown";
                break;
            case 9:
                npart = " affirmative";
                break;
        }

        print(spart + npart);
    }

    private void ball8()
    {
        switch (getRange(1, 9))
        {
            case 1:
                print("Yes இڿڰۣ-ڰۣ—");
                break;
            case 2:
                print("No '(◣_◢)'");
                break;
            case 3:
                print("Never ᶠᶸᶜᵏ♥ᵧₒᵤ");
                break;
            case 4:
                print("Probably ༼ つ ◕_◕ ༽つ");
                break;
            case 5:
                print("Doubtfull ┻━┻︵ \\(°□°)/ ︵ ┻━┻");
                break;
            case 6:
                print("Likely Ƹ̵̡Ӝ̵̨̄Ʒ");
                break;
            case 7:
                print("¸¸♬·¯·♩¸¸♪·¯·♫¸¸Unknown¸¸♬·¯·♩¸¸♪·¯·♫¸¸");
                break;
            case 8:
                print("I don't know ¯\\_(ツ)_/¯");
                break;
            case 9:
                print("Afirmative ✌(◕‿-)✌");
                break;
        }
    }

    private void penguin()
    {
        URL url = null;

        try { url = new URL("http://penguin.wtf/"); }
        catch (MalformedURLException e) { e.printStackTrace(); }

        URLConnection con = null;
        try { con = url.openConnection(); }
        catch (IOException e) { e.printStackTrace(); }

        InputStream in = null;

        try { in = con.getInputStream(); }
        catch (IOException e) { e.printStackTrace(); }

        String encoding = con.getContentEncoding();

        encoding = encoding == null ? "UTF-8" : encoding;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        byte[] buf = new byte[8192];

        int len = 0;

        try { while ((len = in.read(buf)) != -1) { baos.write(buf, 0, len); } }
        catch (IOException e) { e.printStackTrace(); }

        String body = null;

        try { body = new String(baos.toByteArray(), encoding); }
        catch (UnsupportedEncodingException e) { e.printStackTrace(); }

        print(body);
    }

    private void godwhy()
    {
        print(
                "░▄▀▀▀▀▄░█░░█░░░░▄▀▀█░▄▀▀▀▀▄░█▀▀▄ \n" +
                        "░█░░░░█░█░░█░░░░█░░░░█░░░░█░█░░█ \n" +
                        "░█░░░░█░█▀▀█░░░░█░▄▄░█░░░░█░█░░█ \n" +
                        "░▀▄▄▄▄▀░█░░█░░░░▀▄▄█░▀▄▄▄▄▀░█▄▄▀ \n" +
                        "\n" +
                        "░░░░░░░░░▄██████▀▀▀▀▀▀▄\n" +
                        "░░░░░▄█████████▄░░░░░░░▀▀▄▄\n" +
                        "░░▄█████████████░░░░░░░░░░░▀▀▄\n" +
                        "▄██████████████░▄▀░░���▀▄░▀▄▄▄░░▀▄\n" +
                        "███████████████░░▄▀░▀▄▄▄▄▄▄░░░░█\n" +
                        "█████████████████▀█░░▄█▄▄▄░░░░░░█\n" +
                        "███████████░░█▀█░░▀▄░█░█░█░░░░░░░█\n" +
                        "████████████████░░░▀█░▀██▄▄░░░░░░█\n" +
                        "█████████████████░░▄░▀█▄░░░░░▄░░░█\n" +
                        "█████████████████▀███▀▀░▀▄░░░░█░░█\n" +
                        "████████████████░░░░░░░░░░█░░▄▀░░█\n" +
                        "████████████████▄▀▀▀▀▀▀▄░░█░░░░░░█\n" +
                        "████████████████▀▀▀▀▀▀▀▄░░█░░░░░░█\n" +
                        "▀████████████████▀▀▀▀▀▀░░░░░░░░░░█\n" +
                        "░░███████████████▀▀░░░░░���░░░░░░▄▀\n" +
                        "░░▀█████████████░░░░░░░░█░░░░▄▀\n" +
                        "░░░░▀████████████▄░░░▄▄█▀░▄█▀\n" +
                        "░░░░░░▀████████████▀▀▀░░▄███\n" +
                        "░░░░░░████████████████████░█\n" +
                        "░░░░░████████████████████░░█\n" +
                        "░░░░████████████████████░░░█\n" +
                        "░░░░██████████████████░░░░░█\n" +
                        "░░░░██████████████████░░░░░█\n" +
                        "░░░░██████████████████░░░░░█\n" +
                        "░░░░██████████████████░░░░░█\n" +
                        "░░░░██████████████████▄▄▄▄▄█\n" +
                        "\n" +
                        "░░░░░░░░░░░░░█░░░░░█░█░░█░█░░░█\n" +
                        "░░░░░░░░░░░░░█░░░░░█░█░░█░▀█░█▀\n" +
                        "░░░░░░░░░░░░░█░▄█▄░█░█▀▀█░░▀█▀\n" +
                        "░░░░░░░░░░░░░██▀░▀██░█░░█░░░█"
        );
    }

    private void notbad()
    {
        print(
                "░░█▀░░░░░░░░░░░▀▀███████░░░░░ \n" +
                        "░░█▌░░░░░░░░░░░░░░░▀██████░░░ \n" +
                        "░█▌░░░░NOT░BAD░░░░░  ███████░░░ \n" +
                        "░█░░░░░░░░░░░░░░░░░████████░░ \n" +
                        "▐▌░░░░░░░░░░░░░░░░░▀██████▌░░ \n" +
                        "░▌▄███▌░░░░▀████▄░░░░▀████▌░░ \n" +
                        "▐▀▀▄█▄░▌░░░▄██▄▄▄▀░░░░████▄▄░ \n" +
                        "▐░▀░░═▐░░░░░░══░░▀░░░░▐▀░▄▀▌▌ \n" +
                        "▐░░░░░▌░░░░░░░░░░░░░░░▀░▀░░▌▌ \n" +
                        "▐░░░▄▀░░░▀░▌░░░░░░░░░░░░▌█░▌▌ \n" +
                        "░▌░░▀▀▄▄▀▀▄▌▌░░░░░░░░░░▐░▀▐▐░ \n" +
                        "░▌░░▌░▄▄▄▄░░░▌░░░░░░░░▐░░▀▐░░ \n" +
                        "░█░▐▄██████▄░▐░░░░░░░░█▀▄▄▀░░ \n" +
                        "░▐░▌▌░░░░░░▀▀▄▐░░░░░░█▌░░░░░░ \n" +
                        "░░█░░▄▀▀▀▀▄░▄═╝▄░░░▄▀░▌░░░░░░ \n" +
                        "░░░▌▐░░░░░░▌░▀▀░░▄▀░░▐░░░░░░░ \n" +
                        "░░░▀▄░░░░░░░░░▄▀▀░░░░█░░░░░░░ \n" +
                        "░░░▄█▄▄▄▄▄▄▄▀▀░░░░░░░▌▌░░░░░░ \n" +
                        "░░▄▀▌▀▌░░░░░░░░░░░░░▄▀▀▄░░░░░ \n" +
                        "▄▀░░▌░▀▄░░░░░░░░░░▄▀░���▌░▀▄░░░ \n" +
                        "░░░░▌█▄▄▀▄░░░░░░▄▀░░░░▌░░░▌▄▄ \n" +
                        "░░░▄▐██████▄▄░▄▀░░▄▄▄▄▌░░░░▄░ \n" +
                        "░░▄▌████████▄▄▄███████▌░░░░░▄ \n" +
                        "░▄▀░██████████████████▌▀▄░░░░ \n" +
                        "▀░░░█████▀▀░░░▀███████░░░▀▄░░ \n" +
                        "░░░░▐█▀░░░▐░░░░░▀████▌░░░░▀▄░ \n" +
                        "░░░░░░▌░░░▐░░░░▐░░▀▀█░░░░░░░▀ \n" +
                        "░░░░░░▐░░░░▌░░░▐░░░░░▌░░░░░░░\uFEFF"
        );
    }

    private void roulette()
    {
        if (m_AllArguments.matches("next"))
        {
            if(bullet == 5)
            {
                print("The gun fires... everything is covered in red.\n" + m_Event.getAuthor().getUsername() + " has died... †R.I.P. \uD83D\uDD2B");
                bullet = 1;
            }
            else if (getRange(1, 6) == 6)
            {
                print("The gun fires... everything is covered in red.\n" + m_Event.getAuthor().getUsername() + " has died... †R.I.P. \uD83D\uDD2B");
                bullet = 1;
            }
            else
            {
                bullet ++;
                print("The gun clicks... there was no bullet.\n" + String.valueOf(bullet) + "/6");
            }
        }
        else
        {
            bullet = 1;
            if (getRange(1, 6) == 6)
            {
                print("The gun fires... everything is covered in red.\n" + m_Event.getAuthor().getUsername() + " has died... †R.I.P. \uD83D\uDD2B");

            }
            else
            {
                print("The gun clicks... there was no bullet.");
            }
        }
    }

    private void baka()
    {
        List<String> bka = new ArrayList<String>();
        for (User u : m_Event.getMessage().getMentionedUsers() ) { bka.add(u.getUsername()); }
        String msg = bka.toString();
        print(msg.substring(1, msg.length() - 1) + ", \uD83D\uDCA2 YOU BAKA!");
    }

    private void rps()
    {
        if (m_AllArguments == null)
        {
            print("Excuse me dear sir or madam but that ain't no rock, paper or scissors");
            return;
        }
        if (!m_AllArguments.equalsIgnoreCase("rock") && !m_AllArguments.equalsIgnoreCase("paper") && !m_AllArguments.equalsIgnoreCase("scissors"))
        {
            print("Excuse me dear sir or madam but that ain't no rock, paper or scissors");
        }
        else
        {
            String result = null;
            String ic = "I chose ";
            String choice = null;
            switch(getRange(1, 3))
            {
                case 1:
                    choice = "rock, ";
                    if (m_AllArguments.equalsIgnoreCase("rock"))
                    {
                        result = "tie";
                    }
                    if (m_AllArguments.equalsIgnoreCase("paper"))
                    {
                        result = "I lost";
                    }
                    if (m_AllArguments.equalsIgnoreCase("scissors"))
                    {
                        result = "I won";
                    }
                    break;
                case 2:
                    choice = "paper, ";
                    if (m_AllArguments.equalsIgnoreCase("rock"))
                    {
                        result = "I won";
                    }
                    if (m_AllArguments.equalsIgnoreCase("paper"))
                    {
                        result = "tie";
                    }
                    if (m_AllArguments.equalsIgnoreCase("scissors"))
                    {
                        result = "I lost";
                    }
                    break;
                case 3:
                    choice = "scissors, ";
                    if (m_AllArguments.equalsIgnoreCase("rock"))
                    {
                        result = "I lost";
                    }
                    if (m_AllArguments.equalsIgnoreCase("paper"))
                    {
                        result = "I won";
                    }
                    if (m_AllArguments.equalsIgnoreCase("scissors"))
                    {
                        result = "tie";
                    }
                    break;
            }
            print(ic + choice + result);
        }
    }

    private void stats()
    {
        User usr = m_Event.getJDA().getUserById(m_Event.getJDA().getSelfInfo().getId());
        String game;
        if (usr.getCurrentGame() == null)
        {
            game = "none";
        }
        else {game = usr.getCurrentGame().getName();}
        int nr = 0;
        for(User u : m_Event.getGuild().getUsers() ) {nr ++;}
        int snr = 0;
        int susers = 0;
        for(Guild s : m_Event.getJDA().getGuilds() )
        {
            snr ++;
            for (User su : s.getUsers()) {susers ++;}
        }
        List<String> rls = new ArrayList<String>();
        for(Role r : m_Event.getGuild().getRolesForUser(usr) ) {rls.add(r.getName());}
        print("```ruby\nUsername: " + usr.getUsername() + "\nID: " + usr.getId() + "\nOnline Status: " + usr.getOnlineStatus() + "\nGame: " + game + "\nDiscriminator: " + usr.getDiscriminator() +
                "\nCurrent Server Members: " + nr + "\nServers Connected: " + snr + "\nTotal Users: " + susers + "\nJoin Date:  " +
                m_Event.getGuild().getJoinDateForUser(usr).getDayOfMonth() + "/" + m_Event.getGuild().getJoinDateForUser(usr).getMonthValue() + "/" + m_Event.getGuild().getJoinDateForUser(usr).getYear() + " => " + m_Event.getGuild().getJoinDateForUser(usr).getHour() + ":" + m_Event.getGuild().getJoinDateForUser(usr).getMinute() +  ":" + m_Event.getGuild().getJoinDateForUser(usr).getSecond()
                + "\nRoles: " + rls.toString().substring(1, rls.toString().length() - 1) + "```Avatar: " + usr.getAvatarUrl());
    }

    private void setDefChan()
    {
        List<String> rls = new ArrayList<String>();
        for (Role r : m_Event.getGuild().getRolesForUser(m_Event.getAuthor()))
        {
            rls.add(r.getName());
        }
        if (rls.contains("Russian Overlord"))
        {
            String id = m_Event.getTextChannel().getId();
            BufferedWriter writer = null;
            File defchan = new File("defchan.txt");
            try
            {
                System.out.println(defchan.getCanonicalPath());
                writer = new BufferedWriter(new FileWriter(defchan));
                writer.write(id);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            finally
            {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            print("Default channel has been set");
        }
        else
        {
            print("You don't have the role neded for this command");
        }
    }

    private void isonline()
    {
        boolean error = false;
        JSONObject json = null;
        try {
            json = readJsonFromUrl("https://api.twitch.tv/kraken/streams/" + m_AllArguments);
        }
        catch (IOException e)
        {
            error = true;
        }
        if (json == null)
        {
            print("The given username does not exist");
            error = true;
            return;
        }
        if (json.get("stream").toString().matches("null") && !error && m_AllArguments != null)
        {
            print(m_AllArguments + " is offline");
        }
        else if (!error && m_AllArguments != null)
        {
            print(m_AllArguments + " is online");
        }
    }

    private void rip()
    {
        File rip = new File("Rip.png");
        if (m_AllArguments == null)
        {
            print("†R.I.P. in pieces");
            m_Event.getTextChannel().sendFile(rip,null);
        }
        else
        {
            print("†R.I.P. " + m_AllArguments);
            m_Event.getTextChannel().sendFile(rip, null);
        }
    }

    private void luca()
    {
        switch(getRange(1, 36))
        {
            case 1:
                print("**E una în fața spatelui capului meu -** __***Luca 2016 (CS:GO)***__");
                break;
            case 2:
                print("**La astronomie fă și tu ceva nou, nu știu... te duci sub apă -** __***Luca 2016 (Skype)***__");
                break;
            case 3:
                print("**Am fost înecat de un pedofil când eram mic -** __***Luca 2016 (Skype)***__");
                break;
            case 4:
                print("**Mă simt în depresiunde -** __***Luca 2016 (Skype)***__");
                break;
            case 5:
                print("**Anonim: Folosești un skin? Luca: Îl am pus pe mine -** __***Luca 2016 (Text)***__");
                break;
            case 6:
                print("**Îmi place când scoate sunetele alea când o doare ceva -** __***Luca 2016 (Skype)***__");
                break;
            case 7:
                print("**Gayi nu trebuie să doarmă -** __***Luca 2016 (Skype)***__");
                break;
            case 8:
                print("**Gyakuten Saiban e ca Nekopara doar că cu avocati -** __***Luca 2016 (Skype)***__");
                break;
            case 9:
                print("**Cred că o să imi cumpar chiloți cu Airi -** __***Luca 2016 (Skype)***__");
                break;
            case 10:
                print("**Airi, Airi, Airi, Airi, Airi, Airi, Airi, Airi, Airi, Airi, Airi, Airi, Airi, Airi, Airi, Airi, Airi -** __***Luca 2016 (Airi)***__");
                break;
            case 11:
                print("**De acum în colo mă hrănesc cu Airi -** __***Luca 2016 (Airi dake ga Inai Machi)***__");
                break;
            case 12:
                print("**Deci eu pe pula mea de pisicopulă -** __***Luca 2016 (Skype)***__");
                break;
            case 13:
                print("**Să se audă iarna -** __***Luca 2016 (Skype)***__");
                break;
            case 14:
                print("**Mă-ta era o mamă -** __***Luca 2016 (Skype)***__");
                break;
            case 15:
                print("**Nu am făcut încă animațiile pentru sunet -** __***Luca 2016 (IntelliJ 16)***__");
                break;
            case 16:
                print("**Să-ți pizdulesc o pulă în pipi -** __***Luca 2016 (Skype)***__");
                break;
            case 17:
                print("**Stai puțin că am o pulă de verdeață aici -** __***Luca 2016 (Skype)***__");
                break;
            case 18:
                print("**Uite ce drăguț îi stă simbolul ăla de zoom in în guriță, îți place, nu? Îți place.... -** __***Luca 2016 (Skype)***__");
                break;
            case 19:
                print("**Ar trebui să iți schimbi numele în sărmania și să te muți în sarma -** __***Luca 2016 (Skype)***__");
                break;
            case 20:
                print("**Ce dracu.... pisicuțe!? -** __***Luca 2016 (Skype)***__");
                break;
            case 21:
                print("**Când vrei să îl testezi ca zar să îmi dai codul -** __***Luca 2016 (IntelliJ 16)***__");
                break;
            case 22:
                print("**Eu sunt o insignă, am o inimă de insignă -** __***Luca 2016 (CS:GO)***__");
                break;
            case 23:
                print("**Tu ești un dulap care te duce în Iarnia -** __***Luca 2016 (Skype)***__");
                break;
            case 24:
                print("**Tipa aia e destul de kalawii -** __***Luca 2016 (Anime)***__");
                break;
            case 25:
                print("**Nu mai mânca tăvi -** __***Luca 2016 (Skype)***__");
                break;
            case 26:
                print("**Filmul ăsta se încarcă mai încet decât o pulă într-o pizdă prima oară -** __***Luca 2016 (Skype)***__");
                break;
            case 27:
                print("**Luca: Eu ce fac aici? Anonim: Laba. Luca: Umm..... OK! -** __***Luca 2016 (Skype)***__");
                break;
            case 28:
                print("**Eu nu fac prea multe, construiesc mămici! -** __***Luca 2016 (Skype)***__");
                break;
            case 29:
                print("**Dacă ochii tăi ar fi în spate, ce ai vedea în față? -** __***Luca 2016 (Skype)***__");
                break;
            case 30:
                print("**Ar fi ciudat să mă spăl pe cap cu ea pe Skype -** __***Luca 2016 (Text)***__");
                break;
            case 31:
                print("**Îmi place când sunt români pe CS:GO -** __***Luca 2016 (HiddenLoL)***__");
                break;
            case 32:
                print("**Bă, tu ai mai auzit de sutien care poartă șampon? -** __***Luca 2016 (Skype)***__");
                break;
            case 33:
                print("**Dacă violez un șampon nu intru la închisoare -** __***Luca 2016 (Skype)***__");
                break;
            case 34:
                print("**De ce e important să sugi pula? -** __***Luca 2016 (Skype)***__");
                break;
            case 35:
                print("**Brb, mă duc să îmi cumpăr de mâncare din frigider -** __***Luca 2016 (Skype)***__");
                break;
            case 36:
                print("**Anonim: CS? Luca: Nu pute -** __***Luca 2016 (Text)***__");
                break;
        }
    }

    private void stream()
    {
        if (!m_Event.getGuild().getVoiceStatusOfUser(m_Event.getJDA().getUserById(m_Event.getJDA().getSelfInfo().getId())).inVoiceChannel())
        {
            urlMusic();
        }
        else if(!player.isPlaying())
        {
            urlMusic();
        }
        else if (player.isPlaying())
        {
            RemoteSource rs = new RemoteSource(m_AllArguments, YOUTUBE_DL_LAUNCH_ARGS, FFMPEG_LAUNCH_ARGS);
            player.getAudioQueue().add(rs);
            print("**Added:** ***" + rs.getInfo().getTitle() + "*** - __**[" + player.getCurrentAudioSource().getInfo().getDuration().getHours() + ":" + rs.getInfo().getDuration().getMinutes() + ":" + rs.getInfo().getDuration().getSeconds() + "]**__");
        }
    }

    private void stop()
    {
        player.stop();
        if (!player.getAudioQueue().isEmpty()) {player.getAudioQueue().remove();}
        m_Event.getJDA().getAudioManager(m_Event.getGuild()).setSendingHandler(null);
        m_Event.getJDA().getAudioManager(m_Event.getGuild()).closeAudioConnection();
        print("Stopped...");
    }

    private void urlMusic()
    {
        Boolean exception = false;

        try {
            RemoteSource rs = new RemoteSource(m_AllArguments, YOUTUBE_DL_LAUNCH_ARGS, FFMPEG_LAUNCH_ARGS);
            player.getAudioQueue().add(rs);
        } catch (Exception e) {
            exception = true;
            print("Something went wrong, blame Dddsasul.");
        }

        if (!exception)
        {
            VoiceChannel bot = m_Event.getGuild().getVoiceStatusOfUser(m_Event.getJDA().getUserById(m_Event.getJDA().getSelfInfo().getId())).getChannel();
            VoiceChannel user = m_Event.getGuild().getVoiceStatusOfUser(m_Event.getAuthor()).getChannel();
            if (m_Event.getGuild().getVoiceStatusOfUser(m_Event.getJDA().getUserById(m_Event.getJDA().getSelfInfo().getId())).inVoiceChannel() && user == bot)
            {
                m_Event.getJDA().getAudioManager(m_Event.getGuild()).setSendingHandler(player);
                playT = m_Event.getTextChannel();
                player.play();
            }
            else if (m_Event.getGuild().getVoiceStatusOfUser(m_Event.getJDA().getUserById(m_Event.getJDA().getSelfInfo().getId())).inVoiceChannel() && user != bot)
            {
                m_Event.getJDA().getAudioManager(m_Event.getGuild()).closeAudioConnection();
                m_Event.getJDA().getAudioManager(m_Event.getGuild()).openAudioConnection(m_Event.getGuild().getVoiceStatusOfUser(m_Event.getAuthor()).getChannel());
                m_Event.getJDA().getAudioManager(m_Event.getGuild()).setSendingHandler(player);
                playT = m_Event.getTextChannel();
                player.play();
            }
            else if (m_Event.getGuild().getVoiceStatusOfUser(m_Event.getAuthor()).inVoiceChannel())
            {
                m_Event.getJDA().getAudioManager(m_Event.getGuild()).openAudioConnection(m_Event.getGuild().getVoiceStatusOfUser(m_Event.getAuthor()).getChannel());
                m_Event.getJDA().getAudioManager(m_Event.getGuild()).setSendingHandler(player);
                playT = m_Event.getTextChannel();
                player.play();
            }
            else
            {
                print("You are not connected to any voice channels");
            }
        }
    }

    private void skip()
    {
        if (player.isPlaying())
        {
            player.skipToNext();
            print("Skipped");
            playT.sendMessage("**Playing:** ***" + player.getCurrentAudioSource().getInfo().getTitle() + "*** - __**[" + player.getCurrentAudioSource().getInfo().getDuration().getHours() + ":" + player.getCurrentAudioSource().getInfo().getDuration().getMinutes() + ":" + player.getCurrentAudioSource().getInfo().getDuration().getSeconds() + "]**__");
        }
    }

    private void shuffle()
    {
        if (player.isShuffle())
        {
            player.setShuffle(false);
            print("**Shuffle** __***disabled***__");
        }
        else
        {
            player.setShuffle(true);
            print("**Shuffle** __***enabled***__");
        }
    }

    private void playlist()
    {
        Playlist pst = Playlist.getPlaylist(m_AllArguments);
        for (AudioSource s : pst.getSources())
        {
            player.getAudioQueue().add(s);
        }

        VoiceChannel bot = m_Event.getGuild().getVoiceStatusOfUser(m_Event.getJDA().getUserById(m_Event.getJDA().getSelfInfo().getId())).getChannel();
        VoiceChannel user = m_Event.getGuild().getVoiceStatusOfUser(m_Event.getAuthor()).getChannel();
        if (m_Event.getGuild().getVoiceStatusOfUser(m_Event.getJDA().getUserById(m_Event.getJDA().getSelfInfo().getId())).inVoiceChannel() && user == bot)
        {
            m_Event.getJDA().getAudioManager(m_Event.getGuild()).setSendingHandler(player);
            playT = m_Event.getTextChannel();
            player.play();
        }
        else if (m_Event.getGuild().getVoiceStatusOfUser(m_Event.getJDA().getUserById(m_Event.getJDA().getSelfInfo().getId())).inVoiceChannel() && user != bot)
        {
            m_Event.getJDA().getAudioManager(m_Event.getGuild()).closeAudioConnection();
            m_Event.getJDA().getAudioManager(m_Event.getGuild()).openAudioConnection(m_Event.getGuild().getVoiceStatusOfUser(m_Event.getAuthor()).getChannel());
            m_Event.getJDA().getAudioManager(m_Event.getGuild()).setSendingHandler(player);
            playT = m_Event.getTextChannel();
            player.play();
        }
        else if (m_Event.getGuild().getVoiceStatusOfUser(m_Event.getAuthor()).inVoiceChannel())
        {
            m_Event.getJDA().getAudioManager(m_Event.getGuild()).openAudioConnection(m_Event.getGuild().getVoiceStatusOfUser(m_Event.getAuthor()).getChannel());
            m_Event.getJDA().getAudioManager(m_Event.getGuild()).setSendingHandler(player);
            playT = m_Event.getTextChannel();
            player.play();
        }
        else
        {
            print("You are not connected to any voice channels");
        }
    }

    private void queue()
    {
        print(player.getAudioQueue().toString());
    }

    private void defmsg(String ms)
    {
        try{
            DataInputStream dis = new DataInputStream (new FileInputStream ("defchan.txt"));

            byte[] datainBytes = new byte[dis.available()];
            dis.readFully(datainBytes);
            dis.close();

            String cnt = new String(datainBytes, 0, datainBytes.length);

            m_Event.getJDA().getTextChannelById(cnt).sendMessage(ms);

        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }

    private void setDefRole()
    {
        List<String> rlu = new ArrayList<String>();
        for (Role r : m_Event.getGuild().getRolesForUser(m_Event.getAuthor()))
        {
            rlu.add(r.getName());
        }
        List<String> rls = new ArrayList<String>();
        for (Role r : m_Event.getGuild().getRoles())
        {
            rls.add(r.getName());
        }
        if (rlu.contains("Russian Overlord"))
        {
            if (rls.contains(m_AllArguments))
            {
                String rl = m_AllArguments;
                BufferedWriter writer = null;
                File defrole = new File("defrole.txt");
                try
                {
                    System.out.println(defrole.getCanonicalPath());
                    writer = new BufferedWriter(new FileWriter(defrole));
                    writer.write(rl);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                finally
                {
                    try {
                        writer.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                print("Default role has been set");
            }
            else
            {
                print("That role does not exist");
            }
        }
        else
        {
            print("You don't have the role neded for this command");
        }
    }

    private void fuckyou()
    {
        List<String> rls = new ArrayList<String>();
        for (Role r : m_Event.getGuild().getRolesForUser(m_Event.getAuthor()))
        {
            rls.add(r.getName());
        }
        if (rls.contains("Rude"))
        {
            print("https://media.giphy.com/media/480cxAzkCY492/giphy.gif");
        }
    }

    private void silentid()
    {
        for (User u : m_Event.getJDA().getUsersByName(m_AllArguments))
        {
            print(u.getUsername() +" - ID: `" + u.getId() + "` - Discriminator: `" + u.getDiscriminator() + "`");
        }
    }

    private void silentavatar()
    {
        print("The avatar link is: " + m_Event.getJDA().getUserById(m_AllArguments).getAvatarUrl());
    }

    private void veganhunt() {
        switch(getRange(0, 10)) {
            case 0:
                print(m_Event.getAuthor().getUsername() + " got all of us cornered by the vegans and converted into thoes creatures. ***Score:*** __***" + score + "***__");
                score = 0;
                break;
            case 1:
                score = score + 1;
                print(m_Event.getAuthor().getUsername() + " added one ~~victim~~ volunteer to our pile of dead people. ***Current Score:*** __***" + score + "***__");
                break;
            case 2:
                score = score + 2;
                print(m_Event.getAuthor().getUsername() + " made two new sacrifices. ***Current Score:*** __***" + score + "***__");
                break;
            case 3:
                score = score + 3;
                print(m_Event.getAuthor().getUsername() + " poisened three of thoes grass eaters ***Current Score:*** __***" + score + "***__");
                break;
            case 4:
                score = score + 4;
                print(m_Event.getAuthor().getUsername() + " pushed four of thoes things from a building. ***Current Score:*** __***" + score + "***__");
                break;
            case 5:
                score = score + 5;
                print(m_Event.getAuthor().getUsername() + " eviscerated five of thoes green monsters. ***Current Score:*** __***" + score + "***__");
                break;
            case 6:
                score = score + 6;
                print(m_Event.getAuthor().getUsername() + " blasted the brains of six of our enemies. ***Current Score:*** __***" + score + "***__");
                break;
            case 7:
                score = score + 7;
                print(m_Event.getAuthor().getUsername() + " slaughtered seven plant killers. ***Current Score:*** __***" + score + "***__");
                break;
            case 8:
                score = score + 8;
                print(m_Event.getAuthor().getUsername() + " butchered eight crazy people. ***Current Score:*** __***" + score + "***__");
                break;
            case 9:
                score = score + 9;
                print(m_Event.getAuthor().getUsername() + " got crazy about it and took nine of them. ***Current Score:*** __***" + score + "***__");
                break;
            case 10:
                score = score + 10;
                print(m_Event.getAuthor().getUsername() + " went on a fucking rampaged and killed ten of thoes nasty things. ***Current Score:*** __***" + score + "***__");
                break;
        }
    }


    private void test()
    {
        if (!m_Event.getMessage().getMentionedUsers().isEmpty())
        {
            List<String> users = new ArrayList<String>();

            for (User usrs : m_Event.getMessage().getMentionedUsers())
            {
                users.add(usrs.getUsername());
            }

            if (users.size() > 1)
            {
                print("Woah, wait a second, I can't keep track of that many contestants");
            }
            else if (!users.get(0).isEmpty())
            {
                int hpst = 10;
                int hpnd = 10;
                int first = getRange(1, 2);
                String fst = null;
                String snd = null;
                switch (first)
                {
                    case 1:
                        fst = users.get(0);
                        snd = m_Event.getAuthor().getUsername();
                        break;
                    case 2:
                        fst = m_Event.getAuthor().getUsername();
                        snd = users.get(0);
                }
                print("The battle has started and ``" + fst + "`` strikes first.");
                String response = "";

                int d = 1;
                boolean end = false;
                while (!end)
                {
                    if (d == 1)
                    {
                        response = response + "``" + fst + "`` dealt ``" + snd + "`` 3 damage\n";
                        hpnd -= 3;
                        d = 2;
                    }
                    else
                    {
                        response = response + "``" + snd + "`` dealt ``" + fst + "`` 3 damage\n";
                        hpst -= 3;
                        d = 1;
                    }

                    if(hpnd <= 0 || hpst <= 0) end = true;
                }
                print(response);
                print("The result is: ``" + fst + "`` " + hpst + " hp; ``" + snd + "`` " + hpnd + " hp");
            }
        }
        else if (m_Event.getMessage().getMentionedUsers().isEmpty())
        {
            print("What do you want to do? Hit yourself? Go find another participant!");
        }
    }

    private void test2()
    {

    }

    public void test3()
    {

    }

    private void test4()
    {
        List<String> rls = new ArrayList<String>();
        for (Role r : m_Event.getGuild().getRolesForUser(m_Event.getAuthor()))
        {
            rls.add(r.getName());
        }
        if (rls.contains("Russian Overlord"))
        {
            print("Confirmed");
        }
        else
        {
            print("Dennied");
        }
    }

    private void test5()
    {
        File avt = new File("bot.png");
        try {
            AvatarUtil.Avatar avatt = AvatarUtil.getAvatar(avt);
            m_Event.getJDA().getAccountManager().setAvatar(avatt).update();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //m_Event.getJDA().getAccountManager().setGame(m_AllArguments);
    }

    private void test6()
    {
    }





    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
        InputStream is = new URL(url).openStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            JSONObject json = new JSONObject(jsonText);
            return json;
        } finally {
            is.close();
        }
    }

    private void print(String s)
    {
        m_Event.getTextChannel().sendMessage(s);
    }

    private void pPrint(String s)
    {
        m_Event.getPrivateChannel().sendMessage(s);
    }

    public static boolean isInteger(String s) {
        try { Integer.parseInt(s); }
        catch(NumberFormatException e)
        {
            return false;
        }
        catch(NullPointerException e)
        {
            return false;
        }
        // only got here if we didn't return false
        return true;
    }



    private final Random random = new Random();

    public int getRange(int min, int max)
    {
        return random.nextInt(max - min + 1) + min;
    }



    public static DeprecatedDiscordBot get()
    {
        if(m_Bot == null)  m_Bot = new DeprecatedDiscordBot();
        return m_Bot;
    }

    public enum ConnectionAttemptState
    {
        SUCCESSFUL, WRONG_TOKEN, FAILED_TO_CONNECT, ERROR
    }

    public ConnectionAttemptState connect(String token)
    {
        try
        {
            JDA jda = new JDABuilder().setBotToken(token).addListener(this).buildBlocking();
            jda.getAccountManager().setGame("PM †commands");
            player.addEventListener(new PlayerEventListener() {
                public void onEvent(PlayerEvent playerEvent) {
                    if (playerEvent instanceof PlayEvent)
                    {
                        playT.sendMessage("**Playing:** ***" + player.getCurrentAudioSource().getInfo().getTitle() + "*** - __**[" + player.getCurrentAudioSource().getInfo().getDuration().getHours() + ":" + player.getCurrentAudioSource().getInfo().getDuration().getMinutes() + ":" + player.getCurrentAudioSource().getInfo().getDuration().getSeconds() + "]**__");
                    }
                    if (playerEvent instanceof  NextEvent)
                    {
                        playT.sendMessage("**Playing:** ***" + player.getCurrentAudioSource().getInfo().getTitle() + "*** - __**[" + player.getCurrentAudioSource().getInfo().getDuration().getHours() + ":" + player.getCurrentAudioSource().getInfo().getDuration().getMinutes() + ":" + player.getCurrentAudioSource().getInfo().getDuration().getSeconds() + "]**__");
                    }
                }
            });
        }
        catch (LoginException e)
        {
            System.out.println("The provided email / password combination was incorrect. Please provide valid details.");
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        m_Connected = true;

        return ConnectionAttemptState.SUCCESSFUL;
    }

    public static void main (String[] args)
    {

        File file = new File("out.txt");
        Path p1 = Paths.get("out.txt");
        if(file.exists()) {
            try {
                Files.delete(p1);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        PrintStream ps = new PrintStream(fos);
        System.setOut(ps);

        //DeprecatedDiscordBot.get().connect("peeonfacebook@gmail.com", "Gogu1234");
        DeprecatedDiscordBot.get().connect("MTcxOTY3MDI1NTE2NzczMzc2.Cfe8Rg.GTd3Y1PNGM6A4cXxRDoxGc3OK04");

        /*while(true)
        {
         if (DeprecatedDiscordBot.get().up != null)
             if (!DeprecatedDiscordBot.get().up.isPlaying())
             {
                DeprecatedDiscordBot.get().test3();
             }
        }*/


    }
}
