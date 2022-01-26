%%%-------------------------------------------------------------------
%%% @author eirikolav
%%% @copyright (C) 2022, <COMPANY>
%%% @doc
%%%
%%% @end
%%% Created : 20. jan 2022 12:07
%%%-------------------------------------------------------------------
-module(server).
-author("eirikolav").

-behaviour(gen_server).

%% API
-export([start_link/0]).

%% gen_server callbacks
-export([init/1, handle_call/3, handle_cast/2, handle_info/2, terminate/2,
  code_change/3]).

-define(SERVER, ?MODULE).

-record(server_state, {}).

%%%===================================================================
%%% API
%%%===================================================================

%% @doc Spawns the server and registers the local name (unique)
start_link() ->
  gen_server:start_link({local, chat_server}, ?MODULE, [], []).

%%%===================================================================
%%% gen_server callbacks
%%%===================================================================

init([]) ->
  database_logic:initDB(),
  Users = database_logic:getDB(),
  ChatRooms = [],
  {ok, {Users, ChatRooms}}.

%Get all chatrooms available for users
handle_call({newuser}, _From, {Users, ChatRooms}) ->
  Reply = {chatrooms, get_rooms(Users)},
  {reply, Reply, {Users, ChatRooms} };

%Connect user to a chatroom
handle_call({From, connect, ChatRoom, Username}, _From, {Users, ChatRooms}) ->
  Reply = {users, get_username(get_users_in_room(Users, ChatRoom))},
  database_logic:storeDB(ChatRoom, Username, From),
  {reply, Reply, { [{user, ChatRoom, Username, From}| Users], ChatRooms } };

%Send message to all users in a room
handle_call({send, Msg, Room, Username}, _From, {Users, ChatRooms}) ->
  send_message({Msg, Username}, get_users_in_room(Users, Room)),
  {reply, {msg}, {Users, ChatRooms}};

%Leave a room
handle_call({exit, Username}, _From, {Users, ChatRooms}) ->
  User = find_user(Users, Username),
  NewUsers = remove_user(Users, User),
  database_logic:deleteUser(Username),
  {reply, {exit}, {NewUsers, ChatRooms}}.

handle_cast(_Request, State = #server_state{}) ->
  {noreply, State}.

handle_info(_Info, State = #server_state{}) ->
  {noreply, State}.

terminate(_Reason, _State = #server_state{}) ->
  ok.

code_change(_OldVsn, State = #server_state{}, _Extra) ->
  {ok, State}.


%%%===================================================================
%Helper Functions
%%%===================================================================

get_username(Users) ->
  get_username(Users, []).

get_username([], Result) -> Result;
get_username([H|T], Result) ->
  {_, _, Username, _} = H,
  get_username(T, [Username| Result]).

get_users_in_room(Users, ChatRoom) ->
  get_users_in_room(Users, ChatRoom, []).

get_users_in_room([], _, Result) -> Result;
get_users_in_room([H|T], ChatRoom, Result) ->
  {_, CR, _, _ } = H,
  if
    CR == ChatRoom ->
    get_users_in_room(T, ChatRoom, [H|Result]);
    true -> get_users_in_room(T, ChatRoom, Result)
  end.


find_user([H|T], Username) ->
  {_, _, UN, _} = H,
  if
    Username == UN -> H;
    true -> find_user(T, Username)
  end.

remove_user(Users, User) -> remove_user(Users, User, []).

remove_user([], _, Result) -> Result;
remove_user([H|T], User, Result) ->
  if
    User == H -> remove_user(T, User, Result);
    true -> remove_user(T, User, [H|Result])
  end.

get_rooms(Users) ->
  get_rooms(Users, []).

get_rooms([], Result) ->
  Result;

get_rooms([H | T], Result) ->
  {_, Room, _, _} = H,
  case lists:member(Room, Result) of
    true -> get_rooms(T, Result);
    false -> get_rooms(T, [Room|Result])
  end.

send_message(Msg, Users) ->
  lists:foreach(fun({_, _, _, Pid}) -> Pid ! {msg, Msg} end, Users).

