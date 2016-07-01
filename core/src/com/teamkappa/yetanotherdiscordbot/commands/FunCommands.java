package com.teamkappa.yetanotherdiscordbot.commands;

import static com.teamkappa.yetanotherdiscordbot.YetAnotherDiscordBot.*;
import static com.teamkappa.yetanotherdiscordbot.commands.UtilityCommands.print;

/**
 * Created by Sas Luca on 01-Jul-16.
 * Copyright (c) Team Kappa.
 */

public class FunCommands
{
    public static void init()
    {
        COMMANDS.put("ping", FunCommands::ping);
    }

    public static void ping(String args)
    {
        print("pong");
        System.out.println("User: @" + getMessageReceivedEvent().getAuthor().getUsername() + " used command: \"ping\" on server: '" + getMessageReceivedEvent().getGuild().getName() + "' in channel: #" + getMessageReceivedEvent().getTextChannel().getName());
    }
}
