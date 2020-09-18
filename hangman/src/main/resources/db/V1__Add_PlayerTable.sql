CREATE TABLE player
(
    id        UUID PRIMARY KEY,
    name      varchar(128) NOT NULL,
    createdAt bigint NOT NULL
);