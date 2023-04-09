package org.example.Config;

import org.example.Service.Implementation.security.UserDetailsServiceImplementation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
//import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;



@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig {
    @Autowired
    UserDetailsServiceImplementation userDetailsService;

    @Autowired
    private AuthEntryPointJwt unauthorizedHandler;

    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());

        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors().and().csrf().disable();
        http.exceptionHandling().authenticationEntryPoint(unauthorizedHandler).and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .authorizeRequests().antMatchers("/auth/**").permitAll()
                .antMatchers(HttpMethod.GET,"/getAllUsers").hasAnyRole("ADMIN")
                .antMatchers(HttpMethod.GET, "/getUserById/{id}").hasAnyRole("ADMIN","USER")
                .antMatchers(HttpMethod.DELETE, "/deleteUserById/{id}").hasAnyRole("ADMIN")
                .antMatchers(HttpMethod.PUT, "/updateTokens/{id}").hasAnyRole("ADMIN","USER")
                .antMatchers(HttpMethod.GET, "/getUsersSortedByTokensAscending").permitAll()
                .antMatchers(HttpMethod.GET, "/getUsersSortedByTokensDescending").permitAll()
                ////////////////////Quests///////////////////////////////////////
                .antMatchers(HttpMethod.POST, "/createQuest").permitAll()
                .antMatchers(HttpMethod.PUT, "/updateQuest").permitAll()
                .antMatchers(HttpMethod.DELETE, "/deleteQuest/{id}").hasAnyRole("ADMIN")
                .antMatchers(HttpMethod.GET, "/getAllQuests").permitAll()
                .antMatchers(HttpMethod.GET, "/getQuestById/{id}").permitAll()
                .antMatchers(HttpMethod.POST, "/resolveQuest/{idQuest}/{idUser}").permitAll()
                ////////////////////Badges///////////////////////////////////////
                .antMatchers(HttpMethod.POST, "/createBadge").permitAll()
                .antMatchers(HttpMethod.PUT, "/updateBadge").permitAll()
                .antMatchers(HttpMethod.DELETE, "/deleteBadge/{id}").permitAll()
                .antMatchers(HttpMethod.GET, "/getAllBadges").permitAll()
                .antMatchers(HttpMethod.GET, "/getBadgeById/{id}").permitAll()
                .antMatchers(HttpMethod.POST, "/rewardBadge/{idBadge}/{idUser}").permitAll()
                .antMatchers(HttpMethod.GET, "/getBadgesByUserId/{idUser}").permitAll()
                ////////////////////Leaderboard///////////////////////////////////
                .antMatchers(HttpMethod.POST, "/createLeaderboard/{id}").permitAll()
                .antMatchers(HttpMethod.PUT, "/updateLeaderboard").permitAll()
                .antMatchers(HttpMethod.DELETE, "/deleteLeaderboard/{id}").hasAnyRole("ADMIN")
                .antMatchers(HttpMethod.GET, "/getAllLeaderboards").permitAll()
                .antMatchers(HttpMethod.GET, "/getLeaderboardById/{id}").permitAll()
                .anyRequest().authenticated();

        http.authenticationProvider(authenticationProvider());

        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}