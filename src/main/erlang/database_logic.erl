%%%-------------------------------------------------------------------
%%% @author Ferdinand
%%% @copyright (C) 2022, <COMPANY>
%%% @doc
%%%
%%% @end
%%% Created : 23. Jan 2022 12:59
%%%-------------------------------------------------------------------
-module(database_logic).
-author("Ferdinand").
-include_lib("stdlib/include/qlc.hrl").

%% API
-export([initDB/0, storeDB/3, getDB/0, deleteDB/0, deleteUser/1]).

-record(user, {room, userName, pid}).

initDB() ->
  mnesia:create_schema([node()]),
  mnesia:start(),
  mnesia:wait_for_tables([user], 5000),
  try
      mnesia:table_info(type, user)
  catch
      exit: _ ->
        mnesia:create_table(user, [{attributes, record_info(fields, user)},
          {type, bag},
          {disc_copies, [node()]}])

  end.


%Need a mnesia transaction to store records in the database

storeDB(Room, UserName, Pid) ->
  Af = fun() ->
    mnesia:write(#user{room=Room, userName = UserName, pid = Pid})
       end,
  mnesia:transaction(Af).

getDB() ->
  Af = fun() ->
    Query = qlc:q([X || X <- mnesia:table(user)]),
    Results = qlc:e(Query),
    Results
    end,
  {atomic, Users} = mnesia:transaction(Af),
  Users.

deleteDB() ->
  Af = fun() ->
    Query = qlc:q([X || X <- mnesia:table(user)]),
      Results = qlc:e(Query),

      F = fun() ->
        lists:foreach(fun(Result) ->
          mnesia:delete_object(Result) end, Results)
          end,
    mnesia:transaction(F)
end,
  mnesia:transaction(Af).

deleteUser(UserName) ->
  Af = fun() ->
    Query = qlc:q([X || X  <- mnesia:table(user),
      X#user.userName =:= UserName]),
    Results = qlc:e(Query),

    F = fun() ->
      lists:foreach(fun(Result) ->
        mnesia:delete_object(Result) end, Results)
        end,
    mnesia:transaction(F)
       end,
  mnesia:transaction(Af).




