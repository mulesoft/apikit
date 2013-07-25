package ${package}.response;

import java.io.Serializable;
import java.util.Date;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

@JsonAutoDetect
@XmlRootElement(namespace = "http://mulesoft.com/schemas/soccer")
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
public class Match implements Serializable {

    private String homeTeam;
    private String awayTeam;

    @JsonSerialize(using = ${package}.serializer.JsonDateSerializer.class, include=JsonSerialize.Inclusion.NON_NULL)
    private Date date;
    private Integer homeTeamScore;
    private Integer awayTeamScore;

    @JsonProperty
    @XmlElement(required = true, namespace = "http://mulesoft.com/schemas/soccer")
    public String getHomeTeam()
    {
        return homeTeam;
    }

    public void setHomeTeam(String homeTeam)
    {
        this.homeTeam = homeTeam;
    }

    @XmlElement(required = true, namespace = "http://mulesoft.com/schemas/soccer")
    public String getAwayTeam()
    {
        return awayTeam;
    }

    public void setAwayTeam(String awayTeam)
    {
        this.awayTeam = awayTeam;
    }

    @XmlElement(required = true, namespace = "http://mulesoft.com/schemas/soccer")
    public Date getDate()
    {
        return date;
    }

    public void setDate(Date date)
    {
        this.date = date;
    }

    @XmlElement(required = true, namespace = "http://mulesoft.com/schemas/soccer")
    public Integer getHomeTeamScore()
    {
        return homeTeamScore;
    }

    public void setHomeTeamScore(Integer homeTeamScore)
    {
        this.homeTeamScore = homeTeamScore;
    }

    @XmlElement(required = true, namespace = "http://mulesoft.com/schemas/soccer")
    public Integer getAwayTeamScore()
    {
        return awayTeamScore;
    }

    public void setAwayTeamScore(Integer awayTeamScore)
    {
        this.awayTeamScore = awayTeamScore;
    }

}
