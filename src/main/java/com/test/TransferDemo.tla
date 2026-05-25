---- MODULE TransferDemo ----
EXTENDS Integers, TLC, Sequences

(* 这下面是 PlusCal 算法，它看起来像代码，但其实是描述状态转移的宏观蓝图。
   我们定义极其冷酷的初始环境变量。
*)
(* --algorithm Transfer
variables accountA = 100, accountB = 100;

\* 启动两个绝对并发的线程 (Process)，模拟双十一高并发抢占
process TransferThread \in 1..2
variables amount = 50, temp = 0;  \* temp 模拟 CPU 的本地缓存 (L1 Cache / 寄存器)
begin
    \* 动作 1：读取 A 的余额到本地缓存
    ReadA:
        if accountA >= amount then
            temp := accountA;

    \* 动作 2：极其致命！发生线程上下文切换的绝佳地带。修改缓存后写回内存。
    WriteA:
            accountA := temp - amount;

    \* 动作 3：读取 B 的余额
    ReadB:
            temp := accountB;

    \* 动作 4：把钱加给 B
    WriteB:
            accountB := temp + amount;
        end if;
end process;
end algorithm; *)

\* 这一行极其关键！它告诉引擎将上面的伪代码翻译为底层数学状态机
\* BEGIN TRANSLATION
VARIABLES pc, accountA, accountB, amount, temp

vars == << pc, accountA, accountB, amount, temp >>

ProcSet == (1..2)

Init == (* Global variables *)
        /\ accountA = 100
        /\ accountB = 100
        (* Process TransferThread *)
        /\ amount = [self \in 1..2 |-> 50]
        /\ temp = [self \in 1..2 |-> 0]
        /\ pc = [self \in ProcSet |-> "ReadA"]

ReadA(self) == /\ pc[self] = "ReadA"
               /\ IF accountA >= amount[self]
                     THEN /\ temp' = [temp EXCEPT ![self] = accountA]
                          /\ pc' = [pc EXCEPT ![self] = "WriteA"]
                     ELSE /\ pc' = [pc EXCEPT ![self] = "Done"]
                          /\ temp' = temp
               /\ UNCHANGED << accountA, accountB, amount >>

WriteA(self) == /\ pc[self] = "WriteA"
                /\ accountA' = temp[self] - amount[self]
                /\ pc' = [pc EXCEPT ![self] = "ReadB"]
                /\ UNCHANGED << accountB, amount, temp >>

ReadB(self) == /\ pc[self] = "ReadB"
               /\ temp' = [temp EXCEPT ![self] = accountB]
               /\ pc' = [pc EXCEPT ![self] = "WriteB"]
               /\ UNCHANGED << accountA, accountB, amount >>

WriteB(self) == /\ pc[self] = "WriteB"
                /\ accountB' = temp[self] + amount[self]
                /\ pc' = [pc EXCEPT ![self] = "Done"]
                /\ UNCHANGED << accountA, amount, temp >>

TransferThread(self) == ReadA(self) \/ WriteA(self) \/ ReadB(self)
                           \/ WriteB(self)

(* Allow infinite stuttering to prevent deadlock on termination. *)
Terminating == /\ \A self \in ProcSet: pc[self] = "Done"
               /\ UNCHANGED vars

Next == (\E self \in 1..2: TransferThread(self))
           \/ Terminating

Spec == Init /\ [][Next]_vars

Termination == <>(\A self \in ProcSet: pc[self] = "Done")

\* END TRANSLATION

\* =========================================================================
\* 架构师的达摩克利斯之剑：定义系统绝对不可违背的数学宪法 (Invariant)
\* 无论发生什么极其诡异的并发，系统总金额必须永远是 200！
MoneyConservation == accountA + accountB = 200

\* 在代码底部定义一个极其强悍的语义映射视图
StateSemantics ==
    "账户A余额: " \o ToString(accountA) \o "\n" \o
    "账户B余额: " \o ToString(accountB) \o "\n" \o
    "当前危险动作: " \o
    (IF pc = <<"WriteA", "WriteA">> THEN "【警告：双线脏读准备扣款！】"
     ELSE IF pc[1] = "ReadA" THEN "线程1准备读"
     ELSE "执行中...")
=============================================================================
