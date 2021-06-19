package org.triplea.spitfire.server.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import com.github.database.rider.core.api.dataset.DataSet;
import java.net.URI;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.triplea.domain.data.PlayerChatId;
import org.triplea.http.client.HttpInteractionException;
import org.triplea.http.client.lobby.player.PlayerLobbyActionsClient;
import org.triplea.spitfire.server.AllowedUserRole;
import org.triplea.spitfire.server.ProtectedEndpointTest;
import org.triplea.spitfire.server.SpitfireServerTestExtension;

@SuppressWarnings("UnmatchedTest")
@Disabled
@DataSet(value = SpitfireServerTestExtension.LOBBY_USER_DATASET, useSequenceFiltering = false)
class PlayerInfoControllerIntegrationTest extends ProtectedEndpointTest<PlayerLobbyActionsClient> {

  PlayerInfoControllerIntegrationTest(final URI localhost) {
    super(localhost, AllowedUserRole.MODERATOR, PlayerLobbyActionsClient::new);
  }

  @Test
  @DisplayName("Verify fetch player info endpoint is available")
  void fetchPlayerInfo() {
    try {
      verifyEndpoint(client -> client.fetchPlayerInformation(PlayerChatId.of("id")));
    } catch (final HttpInteractionException expected) {
      assertThat(
          "The requested player ID does not exist, we expect a 400 back for this request",
          expected.status(),
          is(400));
    }
  }
}