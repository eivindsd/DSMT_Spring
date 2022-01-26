%%%-------------------------------------------------------------------
%%% @author Ferdinand
%%% @copyright (C) 2022, <COMPANY>
%%% @doc
%%%
%%% @end
%%% Created : 23. Jan 2022 16:23
%%%-------------------------------------------------------------------
-module(user_db).
-author("Ferdinand").

%% API
-export([]).

init() ->
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

start_db() ->
  mnesia:start().