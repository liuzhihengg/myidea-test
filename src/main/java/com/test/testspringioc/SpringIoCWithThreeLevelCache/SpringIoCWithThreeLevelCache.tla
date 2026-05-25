------------------- MODULE SpringIoCWithThreeLevelCache -------------------
EXTENDS TLC, Sequences, FiniteSets

Beans == {"BeanA", "BeanB", "BeanC"}

VARIABLES status, Dependencies, exportedValue, NeedsProxy, singletonFactories

vars == <<status, exportedValue, Dependencies, NeedsProxy, singletonFactories>>

TypeOK ==
    /\ status \in [Beans -> {"UNINITIALIZED", "PARTIALLY_INITIALIZED", "INITIALIZED"}]
    /\ exportedValue \in [Beans -> {"NONE", "RAW", "PROXY"}]
    /\ singletonFactories \in [Beans -> {"NONE", "EXISTS"}]
    /\ NeedsProxy \in SUBSET Beans
    /\ Dependencies \in [Beans -> SUBSET Beans]

Init ==
    /\ status = [b \in Beans |-> "UNINITIALIZED"]
    /\ exportedValue = [b \in Beans |-> "NONE"]
    /\ singletonFactories = [b \in Beans |-> "NONE"]
    /\ NeedsProxy \in SUBSET Beans
    /\ Dependencies \in [Beans -> SUBSET Beans]

Instantiate(b) ==
    /\ status[b] = "UNINITIALIZED"
    /\ status' = [status EXCEPT ![b] = "PARTIALLY_INITIALIZED"]
    /\ singletonFactories' = [singletonFactories EXCEPT ![b] = "EXISTS"]
    /\ UNCHANGED <<exportedValue, Dependencies, NeedsProxy>>

InjectAndComplete(b) ==
    /\ status[b] = "PARTIALLY_INITIALIZED"
    /\ \A d \in Dependencies[b] : status[d] /= "UNINITIALIZED"
    /\ status' = [status EXCEPT ![b] = "INITIALIZED"]
    /\ exportedValue' = [
        exportedValue EXCEPT ![b] = IF b \in NeedsProxy THEN "PROXY" ELSE "RAW"
       ]
    /\ singletonFactories' = [singletonFactories EXCEPT ![b] = "NONE"]
    /\ UNCHANGED <<Dependencies, NeedsProxy>>

(* 核心改进：定义终态动作 (Terminal Action)
   当所有 Bean 都初始化完成后，系统进入“大功告成”状态。
   它不断地执行 UNCHANGED，告诉 TLC：我知道我已经干完活了。
*)
Done ==
    /\ \A b \in Beans : status[b] = "INITIALIZED"
    /\ UNCHANGED vars

(* 逻辑引擎：要么继续初始化，要么原地踏步 *)
Next ==
    \/ \E b \in Beans : Instantiate(b) \/ InjectAndComplete(b)
    \/ Done

Spec == Init /\ [][Next]_vars /\ \A b \in Beans : WF_vars(Instantiate(b)) /\ WF_vars(InjectAndComplete(b))

Consistency ==
    \A target \in Beans :
        (status[target] = "INITIALIZED" /\ target \in NeedsProxy) =>
            ~ \E client \in Beans :
                (target \in Dependencies[client] /\ status[client] = "INITIALIZED"
                 /\ exportedValue[target] /= "PROXY")

AllInitialized == <>(\A b \in Beans : status[b] = "INITIALIZED")
=============================================================================