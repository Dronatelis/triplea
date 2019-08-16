package org.triplea.lobby.server;

import games.strategy.engine.lobby.PlayerEmailValidation;
import games.strategy.engine.lobby.PlayerNameValidation;
import games.strategy.engine.message.IRemoteMessenger;
import games.strategy.engine.message.MessageContext;
import games.strategy.net.INode;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.java.Log;
import org.mindrot.jbcrypt.BCrypt;
import org.triplea.lobby.common.IUserManager;
import org.triplea.lobby.server.db.DatabaseDao;
import org.triplea.lobby.server.db.HashedPassword;

@Log
@AllArgsConstructor
final class UserManager implements IUserManager {
  private final DatabaseDao database;

  void register(final IRemoteMessenger messenger) {
    messenger.registerRemote(this, REMOTE_NAME);
  }

  @Override
  public String updateUser(
      final String username, final String emailAddress, final String hashedPassword) {
    final INode remote = MessageContext.getSender();
    if (!username.equals(remote.getName())) {
      log.severe(
          "Tried to update user permission, but not correct user, username:"
              + username
              + " node:"
              + remote);
      return "Sorry, but I can't let you do that";
    }

    final String validationError =
        Optional.ofNullable(PlayerNameValidation.validate(username))
            .orElseGet(
                () -> emailAddress == null ? null : PlayerEmailValidation.validate(emailAddress));

    if (validationError != null) {
      return validationError;
    }
    final HashedPassword password = new HashedPassword(hashedPassword);

    try {
      database
          .getUserDao()
          .updateUser(
              username,
              emailAddress,
              password.isHashedWithSalt()
                  ? password
                  : new HashedPassword(BCrypt.hashpw(hashedPassword, BCrypt.gensalt())));
    } catch (final IllegalStateException e) {
      return e.getMessage();
    }
    return null;
  }

  @Override
  public String getUserEmail(final String username) {
    final INode remote = MessageContext.getSender();
    if (!username.equals(remote.getName())) {
      log.severe(
          "Tried to get user info, but not correct user, username:" + username + " node:" + remote);
      throw new IllegalStateException("Sorry, but I can't let you do that");
    }
    return database.getUserDao().getUserEmailByName(username);
  }
}
