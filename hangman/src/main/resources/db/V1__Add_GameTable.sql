CREATE TABLE matchup
(
    id        UUID PRIMARY KEY,
    player    UUID,
    state     varchar(128),
    player2   UUID,
    createdAt bigint
);