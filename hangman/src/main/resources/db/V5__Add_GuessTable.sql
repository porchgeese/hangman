CREATE TABLE guess
(
    id        UUID PRIMARY KEY,
    gameId    UUID   NOT NULL REFERENCES game (id),
    playerId  UUID   NOT NULL REFERENCES player (id),
    letter    char   NOT NULL,
    createdAt bigint NOT NULL
);
ALTER TABLE guess
    ADD constraint guess_are_unique UNIQUE (gameId, playerId, letter)