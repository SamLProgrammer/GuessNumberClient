package network;

public enum Requests {// possible requests server can send to Client
	
	CLIENT_JOINED_TO_SERVER, FULL_LOBBY, PING_TO_CLIENT, 
	SEND_INITIAL_WAITING_TIME_TO_CLIENT, SEND_WELLCOME_TO_GAME_TO_CLIENT, NEXT_ROUND_NAMES_TO_CLIENT, 
	SERVER_NOTIFIES_CLIENT_TO_TRY, CLIENT_FAILED, TO_GUESS_NUMBER_IS_SMALLER, TO_GUESS_NUMBER_IS_BIGGER, 
	CONGRATULATION, ON_WAIT_FOR_ROUND_PARTNERS_TO_FINISH, CLIENT_REMOVED, KEEP_ALIVE, TIME_OUT, 
	CLIENT_MOVED_TO_END_OF_QUEUE, CURRENT_ROUND_RANK;
}
