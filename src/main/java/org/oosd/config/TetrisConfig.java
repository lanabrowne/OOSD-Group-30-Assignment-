package org.oosd.config;

public record TetrisConfig(
        //Create record usage to hold data and pass to JSON
        //on off action will be set as boolean, rest of them will
        //be integer
        int fieldWidth,
        int fieldHeight,
        int gameLevel,
        boolean music,
        boolean sfx,
        boolean aiPlay,
        //To set right left game board player type,
        //use enum class of player type
        PlayerType leftPlayer,
        PlayerType rightPlayer,

        boolean extendMode
){
    //Create default setting when user open the application
    public static TetrisConfig defaults()
    {
        return new TetrisConfig(
                10,20,5,
                false,
                false,
                false,
                PlayerType.HUMAN,
                PlayerType.HUMAN,
                false
        );
    }

}


