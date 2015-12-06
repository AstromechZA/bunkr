package com.bunkr_beta.exceptions;

/**
 * Creator: benmeier
 * Created At: 2015-12-06
 */
public class BaseBunkrException extends Exception
{
    public BaseBunkrException(String message, String... args)
    {
        super(String.format(message, args));
    }

    public BaseBunkrException(Throwable e)
    {
        super(e);
    }
}
