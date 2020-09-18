CREATE TABLE gameWord
(
    id        UUID PRIMARY KEY,
    gameId    UUID         NOT NULL REFERENCES game (id),
    playerId  UUID         NOT NULL REFERENCES player (id),
    word      varchar(256) NOT NULL,
    createdAt bigint       NOT NULL
);
ALTER TABLE gameWord
    ADD constraint players_can_only_provide_one_word_per_game UNIQUE (gameId, playerId)