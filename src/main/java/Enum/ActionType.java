package Enum;

public enum  ActionType {
    None,
    //UserController相关
    Login,
    Register,
    //RoomController相关
    CreateRoom,
    ListRoom,
    JoinRoom,
    UpdateRoom,
    QuitRoom,
    //GameController相关
    StartGame,
    ShowTimer,
    StartPlay,
    SyncMovement,
    SyncAnimation,
    SyncArrow,
    TakeDamage,
    SyncHP,
    GameOver,
    QuitInBattle,//
    UpdatePlayerInfo//
}
