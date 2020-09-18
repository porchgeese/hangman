CREATE TABLE matchup (
                         id UUID PRIMARY KEY,
                         player UUID NOT NULL REFERENCES player(id),
                         state varchar(128) NOT NULL,
                         player2 UUID REFERENCES player(id),
                         createdAt bigint NOT NULL
) ;