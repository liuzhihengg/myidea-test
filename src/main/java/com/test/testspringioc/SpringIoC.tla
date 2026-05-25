------------------------ MODULE SpringIoC ------------------------
EXTENDS TLC, Sequences, FiniteSets

(* 理论上的最小完备集合：3个Bean *)
Beans == {"BeanA", "BeanB", "BeanC"}

(* 不再预设答案！
   Dependencies 变成了一个变量。它代表了在某一个平行宇宙中的依赖拓扑图。
*)
VARIABLES status, Dependencies

(* 类型约束：确保映射的合法性 *)
TypeOK ==
    /\ status \in [Beans -> {"UNINITIALIZED", "INITIALIZING", "INITIALIZED"}]
    /\ Dependencies \in [Beans -> SUBSET Beans]

(* 宇宙大爆炸：在第 0 秒分裂出 512 个平行宇宙！
   通过 \in 操作符，TLC 会穷举 [Beans -> SUBSET Beans] 这个函数集合中的所有 512 种可能性，
   并将它们作为 512 种不同的初始状态（Init States）同时开始推演。
*)
Init ==
    /\ status = [b \in Beans |-> "UNINITIALIZED"]
    /\ Dependencies \in [Beans -> SUBSET Beans]

(* 动作 1：实例化。注意增加 UNCHANGED Dependencies，因为依赖关系在运行中是不变的 *)
Instantiate(b) ==
    /\ status[b] = "UNINITIALIZED"
    /\ status' = [status EXCEPT ![b] = "INITIALIZING"]
    /\ UNCHANGED Dependencies

(* 动作 2：注入并完成。前置条件依然严苛：所有下游必须是 INITIALIZED *)
InjectAndComplete(b) ==
    /\ status[b] = "INITIALIZING"
    /\ \A d \in Dependencies[b] : status[d] = "INITIALIZED"
    /\ status' = [status EXCEPT ![b] = "INITIALIZED"]
    /\ UNCHANGED Dependencies

(* 系统步进引擎 *)
Next ==
    \E b \in Beans : Instantiate(b) \/ InjectAndComplete(b)

(* 弱公平性调度 *)
Fairness ==
    \A b \in Beans : WF_status(Instantiate(b)) /\ WF_status(InjectAndComplete(b))

(* 系统宏观规约：现在变量有两个了，所以是 <<status, Dependencies>> *)
Spec == Init /\ [][Next]_<<status, Dependencies>> /\ Fairness

(* 终极期望：不管你是 512 个宇宙中的哪一个，我都要求你最终能把所有 Bean 初始化成功 *)
AllInitialized == <>(\A b \in Beans : status[b] = "INITIALIZED")

=============================================================================