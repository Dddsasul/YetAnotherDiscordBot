package com.teamkappa.yetanotherdiscordbot.commands;

import static com.teamkappa.yetanotherdiscordbot.YetAnotherDiscordBot.*;

/**
 * Created by Sas Luca on 01-Jul-16.
 * Copyright (c) Team Kappa.
 */

public class UtilityCommands
{
    public static void init()
    {
        COMMANDS.put("print", UtilityCommands::print);
    }

    public static void print(String args) { getMessageReceivedEvent().getTextChannel().sendMessage(args); }
}
