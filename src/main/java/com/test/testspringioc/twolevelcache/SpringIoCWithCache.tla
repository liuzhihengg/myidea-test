----------------------- MODULE SpringIoCWithCache -----------------------
EXTENDS TLC, Sequences, FiniteSets

Beans == {"BeanA", "BeanB", "BeanC"}

VARIABLES status, Dependencies

(* 增加一个状态：PARTIALLY_INITIALIZED (二级缓存/提前暴露) *)
TypeOK ==
    /\ status \in [Beans -> {"UNINITIALIZED", "PARTIALLY_INITIALIZED", "INITIALIZED"}]
    /\ Dependencies \in [Beans -> SUBSET Beans]

Init ==
    /\ status = [b \in Beans |-> "UNINITIALIZED"]
    /\ Dependencies \in [Beans -> SUBSET Beans]

(* 动作 1：实例化 (相当于 Spring 的 createBeanInstance)
   只要一实例化，就直接进入 PARTIALLY_INITIALIZED 状态，
   这意味着它现在可以被别人引用了（尽管它自己还没完成注入）。
*)
Instantiate(b) ==
    /\ status[b] = "UNINITIALIZED"
    /\ status' = [status EXCEPT ![b] = "PARTIALLY_INITIALIZED"]
    /\ UNCHANGED Dependencies

(* 动作 2：注入并完成 (关键修改点！)
   以前要求依赖项 d 必须是 INITIALIZED。
   现在只要 d 是 PARTIALLY_INITIALIZED 即可。
   这就是“先胜”的逻辑：允许不完整的个体支撑起系统的运转。
*)
InjectAndComplete(b) ==
    /\ status[b] = "PARTIALLY_INITIALIZED"
    /\ \A d \in Dependencies[b] : status[d] /= "UNINITIALIZED" \* 只要不是没初始化就行
    /\ status' = [status EXCEPT ![b] = "INITIALIZED"]
    /\ UNCHANGED Dependencies

Done ==
    /\ \A b \in Beans : status[b] = "INITIALIZED"
    /\ UNCHANGED <<status, Dependencies>>

Next ==
    \/ \E b \in Beans : Instantiate(b) \/ InjectAndComplete(b)
    \/ Done  \* 加上这个，系统在全员初始化后会进入“自循环”

Fairness ==
    \A b \in Beans : WF_status(Instantiate(b)) /\ WF_status(InjectAndComplete(b))

Spec == Init /\ [][Next]_<<status, Dependencies>> /\ Fairness

(* 终极目标：验证在这个新规约下，是否所有宇宙都能幸存 *)
AllInitialized == <>(\A b \in Beans : status[b] = "INITIALIZED")

=============================================================================