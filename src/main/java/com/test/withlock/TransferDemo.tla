---- MODULE TransferDemo ----
\* 极其神圣的公理引入：必须引入 Sequences 模块才能解锁 \o 拼接算子！
EXTENDS Integers, TLC, Sequences

(* --algorithm Transfer
variables
    accountA = 100,
    accountB = 100,
    \* 物理互斥锁，极其严苛的强类型重构！用整数 0 代表绝对空闲！
    lock = 0;

\* 启动双线程并发宇宙，模拟极限资源抢占
process TransferThread \in 1..2
variables amount = 50, tempA = 0, tempB = 0;
begin
    \* 动作 1：极其刚硬的抢锁阶段（Lock Acquisition）
    GetLock:
        \* 绝对的物理结界：如果锁不是 0，线程必须在此极其屈辱地永久挂起！
        await lock = 0;
        \* 瞬间剥夺其他线程的执行权，将锁刻上自己的线程 ID (1 或 2)
        lock := self;

    \* 动作 2：绝对安全的临界区（Critical Section）
    DoTransfer:
        if accountA >= amount then
            tempA := accountA;
            tempB := accountB;
            \* 在这把锁的保护下，脏写和并发幽灵被物理隔绝
            accountA := tempA - amount;
            accountB := tempB + amount;
        end if;

    \* 动作 3：极其克制的释放阶段（Lock Release）
    ReleaseLock:
        \* 事务执行完毕，必须极其守规矩地将锁状态重置为整数 0
        lock := 0;
end process;
end algorithm; *)

\* -------------------------------------------------------------------------
\* 极其暴烈的物理展开：以下代码为引擎底层所需的绝对数学真理
\* -------------------------------------------------------------------------
\* BEGIN TRANSLATION
VARIABLES accountA, accountB, lock, pc, amount, tempA, tempB

vars == << accountA, accountB, lock, pc, amount, tempA, tempB >>

ProcSet == (1..2)

\* 宇宙大爆炸的极点：一切变量的初始快照
Init == (* Global variables *)
        /\ accountA = 100
        /\ accountB = 100
        /\ lock = 0
        (* Process TransferThread *)
        /\ amount = [self \in 1..2 |-> 50]
        /\ tempA = [self \in 1..2 |-> 0]
        /\ tempB = [self \in 1..2 |-> 0]
        /\ pc = [self \in ProcSet |-> "GetLock"]

\* 状态机流转方程 1：抢锁
GetLock(self) == /\ pc[self] = "GetLock"
                 /\ lock = 0
                 /\ lock' = self
                 /\ pc' = [pc EXCEPT ![self] = "DoTransfer"]
                 /\ UNCHANGED << accountA, accountB, amount, tempA, tempB >>

\* 状态机流转方程 2：执行转账
DoTransfer(self) == /\ pc[self] = "DoTransfer"
                    /\ IF accountA >= amount[self]
                          THEN /\ tempA' = [tempA EXCEPT ![self] = accountA]
                               /\ tempB' = [tempB EXCEPT ![self] = accountB]
                               /\ accountA' = tempA'[self] - amount[self]
                               /\ accountB' = tempB'[self] + amount[self]
                          ELSE /\ UNCHANGED << accountA, accountB, tempA, tempB >>
                    /\ pc' = [pc EXCEPT ![self] = "ReleaseLock"]
                    /\ UNCHANGED << lock, amount >>

\* 状态机流转方程 3：释放锁
ReleaseLock(self) == /\ pc[self] = "ReleaseLock"
                     /\ lock' = 0
                     /\ pc' = [pc EXCEPT ![self] = "Done"]
                     /\ UNCHANGED << accountA, accountB, amount, tempA, tempB >>

\* 线程总状态机
TransferThread(self) == GetLock(self) \/ DoTransfer(self) \/ ReleaseLock(self)

\* 允许系统结束后的无限空转（结巴步），防止物理死锁
Terminating == /\ \A self \in ProcSet: pc[self] = "Done"
               /\ UNCHANGED vars

\* 宇宙时间的绝对齿轮
Next == (\E self \in ProcSet: TransferThread(self))
           \/ Terminating

\* 终极行为规范：起点 + 时序演进
Spec == Init /\ [][Next]_vars
\* END TRANSLATION
\* -------------------------------------------------------------------------

\* =========================================================================
\* 架构师的达摩克利斯之剑：定义系统绝对不可违背的数学宪法 (Safety Invariant)
\* 无论发生什么极其诡异的线程挂起，系统总金额必须永远是 200！
MoneyConservation == accountA + accountB = 200

\* =========================================================================
\* 极其高阶的中文语义映射视图 (ALIAS)
\* 注意这里极其森严的括号 () 结界，强行隔离了 IF 语句的极其低下的优先级！
StateSemantics ==
    "==== 宇宙物理快照 ====\n" \o
    "账户 A 余额 : " \o ToString(accountA) \o "\n" \o
    "账户 B 余额 : " \o ToString(accountB) \o "\n" \o
    "当前锁的状态: " \o
    (IF lock = 0 THEN "【极其空闲】"
     ELSE "【被线程 " \o ToString(lock) \o " 物理独占！】") \o "\n" \o
    "线程 1 位置 : " \o pc[1] \o "\n" \o
    "线程 2 位置 : " \o pc[2]
=============================================================================