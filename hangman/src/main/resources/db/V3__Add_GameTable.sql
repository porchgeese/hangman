CREATE TABLE game
(
    id        UUID PRIMARY KEY,
    matchupId UUID,
    state     varchar(128) NOT NULL,
    createdAt bigint       NOT NULL
);