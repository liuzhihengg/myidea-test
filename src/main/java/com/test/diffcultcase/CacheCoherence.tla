---- MODULE CacheCoherence ----
\* 极其神圣的公理引入：序列算子与引擎探针
EXTENDS Integers, TLC, Sequences

(* --algorithm CacheCoherence
variables
    \* 极其纯净的底层数据库，初始值为 0
    db = 0,
    \* 极其干净的缓存状态，用 -1 代表 null (未命中)
    cache = -1;

\* =========================================================
\* 读请求线程 (Reader)：极其极其渴望读取数据
\* =========================================================
process Reader = 1
variables local_val = 0; \* 极其私有的 L1 缓存，极其容易变成脏数据
begin
    ReadCache:
        if cache = -1 then
            ReadDB:
                \* 模拟极其极其漫长的数据库 I/O
                local_val := db;
            WriteCache:
                \* 极其极其致命的延后写入动作！如果在此之前被挂起，必将酿成大祸！
                cache := local_val;
        end if;
end process;

\* =========================================================
\* 写请求线程 (Writer)：极其极其暴力的修改者
\* =========================================================
process Writer = 2
begin
    UpdateDB:
        \* 极其果断地将真理篡改为 1
        db := 1;
    DeleteCache:
        \* 极其极其守规矩地删除缓存 (恢复为 -1)
        cache := -1;
end process;

end algorithm; *)

\* -------------------------------------------------------------------------
\* 极其暴烈的物理展开：以下代码为引擎底层所需的绝对数学真理
\* (极其极其完整的 TLA+ 状态机，无需再按 Cmd + T)
\* -------------------------------------------------------------------------
\* BEGIN TRANSLATION
VARIABLES db, cache, pc, local_val

vars == << db, cache, pc, local_val >>

ProcSet == {1} \cup {2}

Init == (* Global variables *)
        /\ db = 0
        /\ cache = -1
        (* Process Reader *)
        /\ local_val = 0
        /\ pc = [self \in ProcSet |-> CASE self = 1 -> "ReadCache"
                                        [] self = 2 -> "UpdateDB"]

ReadCache == /\ pc[1] = "ReadCache"
             /\ IF cache = -1
                   THEN /\ pc' = [pc EXCEPT ![1] = "ReadDB"]
                   ELSE /\ pc' = [pc EXCEPT ![1] = "Done"]
             /\ UNCHANGED << db, cache, local_val >>

ReadDB == /\ pc[1] = "ReadDB"
          /\ local_val' = db
          /\ pc' = [pc EXCEPT ![1] = "WriteCache"]
          /\ UNCHANGED << db, cache >>

WriteCache == /\ pc[1] = "WriteCache"
              /\ cache' = local_val
              /\ pc' = [pc EXCEPT ![1] = "Done"]
              /\ UNCHANGED << db, local_val >>

Reader == ReadCache \/ ReadDB \/ WriteCache

UpdateDB == /\ pc[2] = "UpdateDB"
            /\ db' = 1
            /\ pc' = [pc EXCEPT ![2] = "DeleteCache"]
            /\ UNCHANGED << cache, local_val >>

DeleteCache == /\ pc[2] = "DeleteCache"
               /\ cache' = -1
               /\ pc' = [pc EXCEPT ![2] = "Done"]
               /\ UNCHANGED << db, local_val >>

Writer == UpdateDB \/ DeleteCache

\* 允许结束后的空转
Terminating == /\ \A self \in ProcSet: pc[self] = "Done"
               /\ UNCHANGED vars

Next == Reader \/ Writer \/ Terminating

Spec == Init /\ [][Next]_vars
\* END TRANSLATION
\* -------------------------------------------------------------------------

\* =========================================================================
\* 架构师的达摩克利斯之剑：定义系统绝对不可违背的数学宪法 (Safety Invariant)
\* 极其极其严厉的判定：如果缓存不为空 (-1)，它必须绝对等于数据库里的值！
CacheIsConsistent == (cache /= -1) => (cache = db)

\* =========================================================================
\* 极其高阶的中文语义映射视图 (ALIAS)
\* 强行将极其枯燥的整数映射为极具业务冲击力的中文快照！
StateSemantics ==
    "==== 极其血腥的缓存快照 ====\n" \o
    "底层数据库 db: " \o ToString(db) \o "\n" \o
    "高层缓存 cache: " \o (IF cache = -1 THEN "【空】" ELSE ToString(cache)) \o "\n" \o
    "Reader 寄存器: " \o ToString(local_val) \o "\n" \o
    "Reader 动作: " \o pc[1] \o "\n" \o
    "Writer 动作: " \o pc[2]
=============================================================================