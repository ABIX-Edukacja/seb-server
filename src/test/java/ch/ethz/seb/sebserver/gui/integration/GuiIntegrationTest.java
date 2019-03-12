/*
 * Copyright (c) 2019 ETH Zürich, Educational Development and Technology (LET)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package ch.ethz.seb.sebserver.gui.integration;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.HttpURLConnection;

import javax.servlet.http.HttpSession;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import ch.ethz.seb.sebserver.SEBServer;
import ch.ethz.seb.sebserver.gbl.api.JSONMapper;
import ch.ethz.seb.sebserver.gui.service.remote.webservice.auth.OAuth2AuthorizationContextHolder;
import ch.ethz.seb.sebserver.gui.service.remote.webservice.auth.SEBServerAuthorizationContext;
import ch.ethz.seb.sebserver.gui.service.remote.webservice.auth.WebserviceURIService;

@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = SEBServer.class,
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
public abstract class GuiIntegrationTest {

    @Value("${sebserver.webservice.api.admin.clientId}")
    protected String clientId;
    @Value("${sebserver.webservice.api.admin.clientSecret}")
    protected String clientSecret;
    @Value("${sebserver.webservice.api.admin.endpoint}")
    protected String endpoint;

    @Autowired
    protected WebApplicationContext wac;
    @Autowired
    protected JSONMapper jsonMapper;
    @Autowired
    protected FilterChainProxy springSecurityFilterChain;

    protected MockMvc mockMvc;

    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac)
                .addFilter(this.springSecurityFilterChain).build();
    }

    protected OAuth2AuthorizationContextHolder getAuthorizationContextHolder() {

        final HttpSession sessionMock = Mockito.mock(HttpSession.class);
        final WebserviceURIService webserviceURIService = new WebserviceURIService(
                "http", "localhost", "8080", this.endpoint);

        return new OAuth2AuthorizationContextHolder(
                this.clientId,
                this.clientSecret,
                webserviceURIService,
                new SimpleClientHttpRequestFactory() {

                    @Override
                    protected void prepareConnection(
                            final HttpURLConnection connection,
                            final String httpMethod) throws IOException {

                        super.prepareConnection(connection, httpMethod);
                        connection.setInstanceFollowRedirects(false);
                    }
                }) {

            private SEBServerAuthorizationContext authContext = null;

            @Override
            public SEBServerAuthorizationContext getAuthorizationContext() {
                if (this.authContext == null) {
                    this.authContext = super.getAuthorizationContext(sessionMock);
                }
                return this.authContext;
            }
        };
    }

    protected SEBServerAuthorizationContext getAuthorizationContext() {
        final SEBServerAuthorizationContext authorizationContext =
                getAuthorizationContextHolder().getAuthorizationContext();
        assertNotNull(authorizationContext);
        return authorizationContext;

    }

    protected OAuth2AuthorizationContextHolder login(final String name, final String pwd) {
        final OAuth2AuthorizationContextHolder authorizationContextHolder = getAuthorizationContextHolder();
        final SEBServerAuthorizationContext authorizationContext = authorizationContextHolder.getAuthorizationContext();
        if (authorizationContext.isLoggedIn()) {
            throw new IllegalStateException("another user is already logged in");
        }
        authorizationContext.login(name, pwd);
        return authorizationContextHolder;
    }

}