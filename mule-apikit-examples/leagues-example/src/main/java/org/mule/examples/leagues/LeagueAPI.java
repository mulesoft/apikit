/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.examples.leagues;

import org.mule.examples.leagues.exceptions.ConflictException;
import org.mule.examples.leagues.model.League;
import org.mule.examples.leagues.request.Score;
import org.mule.examples.leagues.request.UpdateTeam;
import org.mule.examples.leagues.response.Fixture;
import org.mule.examples.leagues.response.Match;
import org.mule.examples.leagues.response.Position;
import org.mule.examples.leagues.response.Positions;
import org.mule.examples.leagues.response.Team;
import org.mule.examples.leagues.response.Teams;
import org.mule.module.apikit.exception.MuleRestException;
import org.mule.module.apikit.exception.NotFoundException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LeagueAPI {

    private League league;

    public void initialize() {
        league = new League();
        league.initialize();
    }


    public void deleteTeam(String teamId) throws MuleRestException {
        if(!league.hasTeam(teamId)) {
            throw new NotFoundException("Team " + teamId + " does not exist");
        }
        league.deleteTeam(teamId);
    }

    public Teams getTeams(String city) {
        List<Team> teamsList = new ArrayList<Team>();
        List<org.mule.examples.leagues.model.Team> teams = league.getTeams(city);
        for(org.mule.examples.leagues.model.Team team : teams) {
            Team teamResp = new Team();
            teamResp.setId(team.getId());
            teamResp.setName(team.getName());
            teamResp.setHomeCity(team.getHomeCity());
            teamResp.setStadium(team.getStadium());
            teamsList.add(teamResp);
        }

        Teams teamsResp = new Teams();
        teamsResp.setTeams(teamsList);
        return teamsResp;
    }

    public Match getMatch(String homeTeamId, String awayTeamId) throws MuleRestException {
        if(!league.hasMatch(homeTeamId, awayTeamId)) {
            throw new NotFoundException("There is no match between team " + homeTeamId + " and team " + awayTeamId);
        }

        org.mule.examples.leagues.model.Match match = league.getMatch(homeTeamId, awayTeamId);
        Match matchResp = new Match();
        matchResp.setHomeTeam(match.getHomeTeam().getId());
        matchResp.setAwayTeam(match.getAwayTeam().getId());
        matchResp.setDate(match.getDate());
        matchResp.setHomeTeamScore(match.getHomeTeamScore());
        matchResp.setAwayTeamScore(match.getAwayTeamScore());
        return matchResp;
    }

    public Team getTeam(String teamId) throws MuleRestException {
        if(!league.hasTeam(teamId)) {
            throw new NotFoundException("Team " + teamId + " does not exist");
        }

        org.mule.examples.leagues.model.Team team = league.getTeam(teamId);
        Team teamResp = new Team();
        teamResp.setId(team.getId());
        teamResp.setName(team.getName());
        teamResp.setHomeCity(team.getHomeCity());
        teamResp.setStadium(team.getStadium());
        teamResp.setMatches(team.getMatchesPlayed());
        return teamResp;
    }

    public Positions getPositions() throws MuleRestException {
        Positions positions = new Positions();
        List<org.mule.examples.leagues.model.Team> teams = league.orderTeamsByPosition();
        List<Position> positionsList = new ArrayList<Position>();

        int positionIndex = 1;
        for(org.mule.examples.leagues.model.Team team : teams) {
            Position position = new Position();
            position.setPosition(positionIndex);
            position.setTeam(team.getId());
            position.setPoints(team.getPoints());
            position.setMatchesPlayed(team.getMatchesPlayed());
            position.setMatchesWon(team.getMatchesWon());
            position.setMatchesLost(team.getMatchesLost());
            position.setMatchesDraw(team.getMatchesDraw());
            position.setGoalsAgainst(team.getGoalsAgainst());
            position.setGoalsInFavor(team.getGoalsInFavor());
            positionsList.add(position);
            positionIndex++;
        }

        positions.setPositions(positionsList);
        return positions;
    }

    public Fixture getFixture() {
        Fixture fixture = new Fixture();
        List<org.mule.examples.leagues.model.Match> matches = league.getMatches();
        List<Match> matchesList = new ArrayList<Match>();

        for(org.mule.examples.leagues.model.Match match : matches) {
            Match matchResp = new Match();
            matchResp.setHomeTeam(match.getHomeTeam().getId());
            matchResp.setAwayTeam(match.getAwayTeam().getId());
            matchResp.setDate(match.getDate());
            matchResp.setHomeTeamScore(match.getHomeTeamScore());
            matchResp.setAwayTeamScore(match.getAwayTeamScore());
            matchesList.add(matchResp);
        }

        fixture.setFixture(matchesList);
        return fixture;
    }

    public String addTeam(org.mule.examples.leagues.request.Team team) throws MuleRestException {
        if(league.hasTeam(team.getId())) {
            throw new ConflictException("There is already a team with id " + team.getId());
        }

        org.mule.examples.leagues.model.Team newTeam = new org.mule.examples.leagues.model.Team();
        newTeam.setId(team.getId());
        newTeam.setName(team.getName());
        newTeam.setHomeCity(team.getHomeCity());
        newTeam.setStadium(team.getStadium());

        league.addTeam(newTeam);
        return team.getId();
    }

    public void updateScore(String homeTeamId, String awayTeamId, Score score) throws MuleRestException {
        if(!league.hasMatch(homeTeamId, awayTeamId)) {
            throw new NotFoundException("There is no match between team " + homeTeamId + " and team " + awayTeamId);
        }

        org.mule.examples.leagues.model.Match match = league.getMatch(homeTeamId, awayTeamId);
        if(new Date().compareTo(match.getDate()) >= 0) {
            match.setHomeTeamScore(score.getHomeTeamScore());
            match.setAwayTeamScore(score.getAwayTeamScore());
        } else {
            throw new ConflictException("The match between team " + homeTeamId + " and team " + awayTeamId + " has not been played yet");
        }
    }

    public void updateTeam(String teamId, UpdateTeam updateTeam) throws MuleRestException {
        if(!league.hasTeam(teamId)) {
            throw new NotFoundException("Team " + teamId + " does not exist");
        }

        org.mule.examples.leagues.model.Team team = league.getTeam(teamId);
        if(updateTeam.getName() != null) {
            team.setName(updateTeam.getName());
        }

        if(updateTeam.getHomeCity() != null) {
            team.setHomeCity(updateTeam.getHomeCity());
        }

        if(updateTeam.getStadium() != null) {
            team.setStadium(updateTeam.getStadium());
        }
    }

}
