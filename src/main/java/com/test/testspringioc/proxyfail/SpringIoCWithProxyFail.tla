----------------------- MODULE SpringIoCWithProxyFail -----------------------
EXTENDS TLC, Sequences, FiniteSets

Beans == {"BeanA", "BeanB", "BeanC"}

VARIABLES status, Dependencies, exportedValue, NeedsProxy

TypeOK ==
    /\ status \in [Beans -> {"UNINITIALIZED", "PARTIALLY_INITIALIZED", "INITIALIZED"}]
    /\ exportedValue \in [Beans -> {"NONE", "RAW", "PROXY"}]
    /\ NeedsProxy \in SUBSET Beans \* 自动生成所有可能的代理需求组合
    /\ Dependencies \in [Beans -> SUBSET Beans]

Init ==
    /\ status = [b \in Beans |-> "UNINITIALIZED"]
    /\ exportedValue = [b \in Beans |-> "NONE"]
    /\ NeedsProxy \in SUBSET Beans    \* 核心：在这里引爆 8 种代理组合
    /\ Dependencies \in [Beans -> SUBSET Beans] \* 核心：在这里引爆 512 种拓扑

Instantiate(b) ==
    /\ status[b] = "UNINITIALIZED"
    /\ status' = [status EXCEPT ![b] = "PARTIALLY_INITIALIZED"]
    /\ exportedValue' = [exportedValue EXCEPT ![b] = "RAW"]
    /\ UNCHANGED <<Dependencies, NeedsProxy>>

InjectAndComplete(b) ==
    /\ status[b] = "PARTIALLY_INITIALIZED"
    /\ \A d \in Dependencies[b] : status[d] /= "UNINITIALIZED"
    /\ status' = [status EXCEPT ![b] = "INITIALIZED"]
    /\ exportedValue' = [
        exportedValue EXCEPT ![b] = IF b \in NeedsProxy THEN "PROXY" ELSE "RAW"
       ]
    /\ UNCHANGED <<Dependencies, NeedsProxy>>

Next == \E b \in Beans : Instantiate(b) \/ InjectAndComplete(b)

Spec == Init /\ [][Next]_<<status, exportedValue, Dependencies, NeedsProxy>>

(* 我们的一致性断言：绝对不允许 A 被注入了 RAW，但最终变成了 PROXY *)
Consistency ==
    \A b \in Beans :
        (status[b] = "INITIALIZED" /\ b \in NeedsProxy) =>
            \A d \in Beans :
                (b \in Dependencies[d] /\ status[d] = "INITIALIZED") =>
                    FALSE \* 只要发生了这种情况，直接报错，让 TLC 抓现场

\* 修改后的更严谨的 Consistency：
RealConsistency ==
    \A target \in Beans :
        (status[target] = "INITIALIZED" /\ target \in NeedsProxy) =>
            ~ \E client \in Beans :
                (target \in Dependencies[client] /\ status[client] = "INITIALIZED")

AllInitialized == <>(\A b \in Beans : status[b] = "INITIALIZED")
=============================================================================