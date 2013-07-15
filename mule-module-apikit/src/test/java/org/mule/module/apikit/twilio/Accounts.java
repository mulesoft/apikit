package org.mule.module.apikit.twilio;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.util.List;

@JsonAutoDetect
public class Accounts
{

    private List<Account> accounts;

    public List<Account> getAccounts()
    {
        return accounts;
    }

    public void setAccounts(List<Account> accounts)
    {
        this.accounts = accounts;
    }

    public Account getAccount(String sid)
    {
        for (Account account : accounts)
        {
            if (account.getSid().equals(sid))
            {
                return account;
            }
        }
        return null;
    }

}
