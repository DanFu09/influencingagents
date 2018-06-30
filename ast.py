#!/usr/bin/env python3

import random

mutation_rate = 0.1

constants = [
    "direction",
    "distance",
    "heading",
    "influence",
    "index",
    "heading",
    "goal",
    "acc",
    "const"  # some constant literal
]

triops = [
    "gez"
]

binops = [
    "add",
    "sub",
    "mul",
    "exp"
]

unops = [
    "sin",
    "cos",
    "asin",
    "acos"
]

nodes = constants + triops + binops + unops


class Const:
    def __init__(self, val):
        self.val = val

    def __str__(self):
        return str(self.val)

    def copy(self):
        return Const(self.val)

    def nodes(self):
        return [self]


class Triop:
    def __init__(self, op, cond, e1, e2):
        self.op = op
        self.cond = cond
        self.e1 = e1
        self.e2 = e2

    def __str__(self):
        return f"({self.op} {self.cond} {self.e1} {self.e2})"

    def copy(self):
        copy_op = getattr(self.cond, "copy", None)
        cond = self.cond
        if callable(copy_op):
            cond = cond.copy()
        copy_op = getattr(self.e1, "copy", None)
        e1 = self.e1
        if callable(copy_op):
            e1 = e1.copy()
        copy_op = getattr(self.e2, "copy", None)
        e2 = self.e2
        if callable(copy_op):
            e2 = e2.copy()
        return Triop(self.op, cond, e1, e2)

    def nodes(self):
        return [self] + self.cond.nodes() + self.e1.nodes() + self.e2.nodes()


class Binop:
    def __init__(self, op, lhs, rhs):
        self.op = op
        self.lhs = lhs
        self.rhs = rhs

    def __str__(self):
        return f"({self.op} {self.lhs} {self.rhs})"

    def copy(self):
        copy_op = getattr(self.lhs, "copy", None)
        lhs = self.lhs
        if callable(copy_op):
            lhs = lhs.copy()
        copy_op = getattr(self.rhs, "copy", None)
        rhs = self.rhs
        if callable(copy_op):
            rhs = rhs.copy()
        return Binop(self.op, lhs, rhs)

    def nodes(self):
        return [self] + self.lhs.nodes() + self.rhs.nodes()


class Unop:
    def __init__(self, op, arg):
        self.op = op
        self.arg = arg

    def __str__(self):
        return f"({self.op} {self.arg})"

    def copy(self):
        copy_op = getattr(self.arg, "copy", None)
        arg = self.arg
        if callable(copy_op):
            arg = arg.copy()
        return Unop(self.op, arg)

    def nodes(self):
        return [self] + self.arg.nodes()


class Genome:
    def __init__(self, ast, start_goal):
        self.ast = ast
        self.start_goal = start_goal

    def __str__(self):
        acc = "goal" if self.start_goal else "current"
        return f"acc={acc}; {self.ast}"

    def copy(self):
        return Genome(self.ast.copy(), self.start_goal)

    def nodes(self):
        return self.ast.nodes()


def seed_const():
    node = random.choice(constants)
    if node != "const":
        return Const(node)
    return Const(random.random() * 2 - 1)


def seed_ast(constantprob=0.33):
    if random.random() < constantprob:
        node = random.choice(constants)
    else:
        node = random.choice(nodes)
    newconstantprob = (1. + constantprob) / 2.
    if node in triops:
        return Triop(node, seed_ast(newconstantprob), seed_ast(newconstantprob), seed_ast(newconstantprob))
    elif node in binops:
        return Binop(node, seed_ast(newconstantprob), seed_ast(newconstantprob))
    elif node in unops:
        return Unop(node, seed_ast(newconstantprob))
    else:
        return seed_const()


def seed_genome():
    return Genome(seed_ast(), random.random() < 0.5)


def crossover(a, b):
    a, b = a.copy(), b.copy()
    node1 = random.choice(a.nodes())
    node2 = random.choice(b.nodes())
    # MASSIVE HACKS!
    node1.__dict__, node2.__dict__ = node2.__dict__, node1.__dict__
    node1.__class__, node2.__class__ = node2.__class__, node1.__class__
    return (a, b)


def mutate(ast):
    node = random.choice(ast.nodes())
    children = []
    if isinstance(node, Triop):
        children = [node.cond, node.e1, node.e2]
    elif isinstance(node, Binop):
        children = [node.lhs, node.rhs]
    elif isinstance(node, Unop):
        children = [node.arg]
    random.shuffle(children)
    op = random.choice(nodes)
    n_children = 0
    if op in triops:
        while len(children) < 3:
            children.append(seed_ast())
        new_node = Triop(op, children[0], children[1], children[2])
    elif op in binops:
        while len(children) < 2:
            children.append(seed_ast())
        new_node = Binop(op, children[0], children[1])
    elif op in unops:
        while len(children) < 1:
            children.append(seed_ast())
        new_node = Unop(op, children[0])
    else:
        new_node = seed_ast()
    node.__dict__ = new_node.__dict__
    node.__class__ = new_node.__class__
    return ast

def main():
    print("testing crossover")
    ast1 = Binop("mul", Unop("sin", Const("acc")), Const("heading"))
    ast2 = Unop("asin", Binop("sub", Const(.5), Const("direction")))
    for _ in range(10):
        print(ast1, ast2)
        ast1, ast2 = crossover(ast1, ast2)

    print("testing mutation")
    ast = seed_genome()
    ast_str = None
    longest = ""
    gen = 0
    iters = 0
    while True:
        gen += 1
        mutate(ast)
        new_str = str(ast)
        if new_str == ast_str:
            continue
        iters += 1
        ast_str = new_str
        if len(ast_str) > len(longest):
            longest = ast_str
        print(f"{gen}: {ast_str}")
        if iters > 100:
            break
    print(f"longest: {longest}")


if __name__ == "__main__":
    main()
