package org.mule.module.apikit.twilio;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@JsonAutoDetect
public class Account
{
    private String date_created;
    private String date_updated;
    private String friendly_name;
    private String sid;
    private String status;
    private String type;

    public String getDate_created()
    {
        return date_created;
    }

    public void setDate_created(String date_created)
    {
        this.date_created = date_created;
    }

    public String getDate_updated()
    {
        return date_updated;
    }

    public void setDate_updated(String date_updated)
    {
        this.date_updated = date_updated;
    }

    public String getFriendly_name()
    {
        return friendly_name;
    }

    public void setFriendly_name(String friendly_name)
    {
        this.friendly_name = friendly_name;
    }

    public String getSid()
    {
        return sid;
    }

    public void setSid(String sid)
    {
        this.sid = sid;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    @Override
    public String toString()
    {
        return "Account{" +
               "friendly_name='" + friendly_name + '\'' +
               ", sid='" + sid + '\'' +
               ", status='" + status + '\'' +
               ", type='" + type + '\'' +
               '}';
    }
}
