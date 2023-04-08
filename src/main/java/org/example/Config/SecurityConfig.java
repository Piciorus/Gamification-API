package org.example.Config;

import org.example.Domain.Entities.Badge;
import org.example.Domain.Entities.Leaderboard;
import org.example.Domain.Entities.Quest;
import org.example.Domain.Entities.User;
import org.example.Domain.Models.LoginUserRequest;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.web.bind.annotation.*;

@Configuration
@EnableWebSecurity(debug = true)
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable();
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http
                .authorizeRequests()
                ////////////////////Users///////////////////////////////////////
                .antMatchers(HttpMethod.POST, "/register").anonymous()
                .antMatchers(HttpMethod.POST, "/login").anonymous()
                .antMatchers(HttpMethod.GET, "/getAllUsers").anonymous()
                .antMatchers(HttpMethod.GET, "/getUserById/{id}").anonymous()
                .antMatchers(HttpMethod.DELETE, "/deleteUserById/{id}").anonymous()
                .antMatchers(HttpMethod.PUT, "/updateTokens/{id}").anonymous()
                .antMatchers(HttpMethod.GET, "/getUsersSortedByTokensAscending").anonymous()
                .antMatchers(HttpMethod.GET, "/getUsersSortedByTokensDescending").anonymous()
                ////////////////////Quests///////////////////////////////////////
                .antMatchers(HttpMethod.POST, "/createQuest").anonymous()
                .antMatchers(HttpMethod.PUT, "/updateQuest").anonymous()
                .antMatchers(HttpMethod.DELETE, "/deleteQuest/{id}").anonymous()
                .antMatchers(HttpMethod.GET, "/getAllQuests").anonymous()
                .antMatchers(HttpMethod.GET, "/getQuestById/{id}").anonymous()
                .antMatchers(HttpMethod.POST, "/resolveQuest/{idQuest}/{idUser}").anonymous()
                ////////////////////Badges///////////////////////////////////////
                .antMatchers(HttpMethod.POST, "/createBadge").anonymous()
                .antMatchers(HttpMethod.PUT, "/updateBadge").anonymous()
                .antMatchers(HttpMethod.DELETE, "/deleteBadge/{id}").anonymous()
                .antMatchers(HttpMethod.GET, "/getAllBadges").anonymous()
                .antMatchers(HttpMethod.GET, "/getBadgeById/{id}").anonymous()
                .antMatchers(HttpMethod.POST, "/rewardBadge/{idBadge}/{idUser}").anonymous()
                .antMatchers(HttpMethod.GET, "/getBadgesByUserId/{idUser}").anonymous()
                ////////////////////Leaderboard///////////////////////////////////
                .antMatchers(HttpMethod.POST, "/createLeaderboard/{id}").anonymous()
                .antMatchers(HttpMethod.PUT, "/updateLeaderboard").anonymous()
                .antMatchers(HttpMethod.DELETE, "/deleteLeaderboard/{id}").anonymous()
                .antMatchers(HttpMethod.GET, "/getAllLeaderboards").anonymous()
                .antMatchers(HttpMethod.GET, "/getLeaderboardById/{id}").anonymous()
                .anyRequest()
                .authenticated()
                .and()
                .cors()
                .and()
                .formLogin().disable()
                .httpBasic();
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers(HttpMethod.OPTIONS, "/**");
    }
}
