------------------- MODULE NaiveConsensus -------------------
EXTENDS TLC, FiniteSets

Nodes == {"N1", "N2", "N3"}

VARIABLES
    status,
    msgs,
    decision

vars == <<status, msgs, decision>>

TypeOK ==
    /\ status \in [Nodes -> {"READY", "VOTED_YES", "VOTED_NO", "COMMITTED", "ABORTED"}]
    /\ decision \in {"PENDING", "COMMIT", "ABORT"}
    /\ msgs \subseteq [type : {"VOTE", "DECISION"},
                       from : Nodes \cup {"COORDINATOR"},
                       val : {"YES", "NO", "COMMIT", "ABORT"}]

Init ==
    /\ status = [n \in Nodes |-> "READY"]
    /\ msgs = {}
    /\ decision = "PENDING"

VoteYes(n) ==
    /\ status[n] = "READY"
    /\ status' = [status EXCEPT ![n] = "VOTED_YES"]
    /\ msgs' = msgs \cup {[type |-> "VOTE", from |-> n, val |-> "YES"]}
    /\ UNCHANGED decision

(* 恶意修改 2：决策接收逻辑，模拟节点在消息混乱时的“随机”提取 *)
(* 动作：节点极其草率地接收指令 *)
ReceiveDecision(n) ==
    /\ status[n] \in {"VOTED_YES", "VOTED_NO"}
    /\ \E m \in msgs : m.type = "DECISION"
    /\ \E m \in msgs :
        /\ m.type = "DECISION"
        /\ IF m.val = "COMMIT"
           THEN status' = [status EXCEPT ![n] = "COMMITTED"]
           ELSE status' = [status EXCEPT ![n] = "ABORTED"]
    /\ UNCHANGED <<msgs, decision>>

(* 动作：协调者“人格分裂”——不再受 decision 变量约束 *)
TimeoutAbort ==
    /\ \E n \in Nodes : ~ (\E m \in msgs : m.type = "VOTE" /\ m.from = n /\ m.val = "YES")
    /\ decision' = "ABORT"
    /\ msgs' = msgs \cup {[type |-> "DECISION", from |-> "COORDINATOR", val |-> "ABORT"]}
    /\ UNCHANGED status

CommitSuccess ==
    /\ \A n \in Nodes : \E m \in msgs : m.type = "VOTE" /\ m.from = n /\ m.val = "YES"
    /\ decision' = "COMMIT"
    /\ msgs' = msgs \cup {[type |-> "DECISION", from |-> "COORDINATOR", val |-> "COMMIT"]}
    /\ UNCHANGED status

(* 核心补丁：定义终态自旋，消除死锁报警 *)
Done ==
    /\ \A n \in Nodes : status[n] \in {"COMMITTED", "ABORTED"}
    /\ UNCHANGED vars

Next ==
    \/ \E n \in Nodes : VoteYes(n)
    \/ \E n \in Nodes : ReceiveDecision(n)
    \/ TimeoutAbort
    \/ CommitSuccess
    \/ Done

Spec == Init /\ [][Next]_vars

Consistency ==
    ~ (\E n1, n2 \in Nodes : status[n1] = "COMMITTED" /\ status[n2] = "ABORTED")

=============================================================================