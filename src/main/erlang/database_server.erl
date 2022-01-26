%%%-------------------------------------------------------------------
%%% @author Ferdinand
%%% @copyright (C) 2022, <COMPANY>
%%% @doc
%%%
%%% @end
%%% Created : 23. Jan 2022 14:39
%%%-------------------------------------------------------------------
-module(database_server).
-author("Ferdinand").

-include_lib("stdlib/include/qlc.hrl").

-behaviour(gen_server).


%% API
-export([start_link/0, store/3, getDB/0, deleteDB/0]).

%% gen_server callbacks
-export([init/1, handle_call/3, handle_cast/2,  terminate/2,
  code_change/3]).

-define(SERVER, ?MODULE).

-record(database_server_state, {}).
-record(user, {room, userName, pid}).

%%%===================================================================
%%% API
%%%===================================================================


store(Room, UserName, Pid) ->
  gen_server:call({local, ?MODULE}, {store, Room, UserName, Pid}).


getDB() ->
  gen_server:call({local, ?MODULE}, {getDB}).


deleteDB() ->
  gen_server:call({local, ?MODULE}, {deleteDB}).


%% @doc Spawns the server and registers the local name (unique)
-spec(start_link() ->
  {ok, Pid :: pid()} | ignore | {error, Reason :: term()}).
start_link() ->
  gen_server:start_link({local, database_server}, ?MODULE, [], []).

%%%===================================================================
%%% gen_server callbacks
%%%===================================================================

%% @private
%% @doc Initializes the server
init([]) ->
  mnesia:create_schema([node()]),
  mnesia:start(),
  try
    mnesia:table_info(type, user)
  catch
    exit: _ ->
      mnesia:create_table(user, [{attributes, record_info(fields, user)},
        {type, bag},
        {disc_copies, [node()]}])
  end,
  {ok, []}.


handle_call({store, Room, UserName, Pid}, _From, State) ->
  Af = fun() ->
    mnesia:write(#user{room=Room, userName = UserName, pid = Pid})
       end,
  mnesia:transaction(Af),
  {reply, ok, State};


handle_call(getDB, _From, State) ->
  Af = fun() ->
    Query = qlc:q([X || X <- mnesia:table(user)]),
    Results = qlc:e(Query),
    Results
       end,
  {atomic, Users} = mnesia:transaction(Af),
  Users,
  {reply, Users, State};

handle_call(delete, _From, State) ->
  Af = fun() ->
    Query = qlc:q([X || X <- mnesia:table(user)]),
    Results = qlc:e(Query),

    F = fun() ->
      lists:foreach(fun(Result) ->
        mnesia:delete_object(Result) end, Results)
        end,
    mnesia:transaction(F)
       end,
  mnesia:transaction(Af),
  {reply, ok, State}.

terminate(_Reason, _State = #database_server_state{}) ->
  ok.

%% @private
%% @doc Convert process state when code is changed
-spec(code_change(OldVsn :: term() | {down, term()}, State :: #database_server_state{},
    Extra :: term()) ->
  {ok, NewState :: #database_server_state{}} | {error, Reason :: term()}).
code_change(_OldVsn, State = #database_server_state{}, _Extra) ->
  {ok, State}.

%%%===================================================================
%%% Internal functions
%%%===================================================================


handle_cast(_Arg0, _Arg1) ->
  erlang:error(not_implemented).