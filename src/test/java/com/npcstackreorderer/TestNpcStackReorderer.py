BIG_NPC_STACK_DRAW_LIMIT = 5

def test2(stack, hidePlayers = False):
    hide = []

    numberOfVisibleBiggerThan1x1Npcs = 0
    numberOfVisible1x1Npcs = 0 # TODO
    numberOfHiddenNpcs = 0
    binaryNumberOf1x1NpcsAmongFirst5 = 0
    binaryNumberOfPlayers = 0

    stopCounting = BIG_NPC_STACK_DRAW_LIMIT

    for i in range(len(stack)):
        if i < BIG_NPC_STACK_DRAW_LIMIT:
            if stack[i][1] == "P": # Player
                if binaryNumberOfPlayers == 0:
                    binaryNumberOfPlayers += 1
                else:
                    stopCounting += 1
            elif stack[i][1] == "N" and int(stack[i][0]) <= 1: # size 1x1 NPC
                if binaryNumberOf1x1NpcsAmongFirst5 == 0:
                    binaryNumberOf1x1NpcsAmongFirst5 += 1
                else:
                    stopCounting += 1
        if i < stopCounting:
            if stack[i][1] == "N" and int(stack[i][0]) > 1: # size 2x2 or bigger NPC
                numberOfVisibleBiggerThan1x1Npcs += 1
        elif stack[i][1] == "N": # any size NPC
            numberOfHiddenNpcs += 1

    print("\nnumberOfHiddenNpcs =", numberOfHiddenNpcs,
          "\nnumberOfVisibleBiggerThan1x1Npcs =", numberOfVisibleBiggerThan1x1Npcs,
          "\nbinaryNumberOfPlayers =", binaryNumberOfPlayers,
          "\nbinaryNumberOf1x1NpcsAmongFirst5 =", binaryNumberOf1x1NpcsAmongFirst5)
    if numberOfHiddenNpcs > 0 and numberOfVisibleBiggerThan1x1Npcs >= (
        BIG_NPC_STACK_DRAW_LIMIT - binaryNumberOfPlayers - binaryNumberOf1x1NpcsAmongFirst5):
        numberOfHidden = 0
        for i in range(len(stack)):
            if numberOfHidden >= numberOfHiddenNpcs:
                break
            
            if stack[i][1] == "P" and not hidePlayers:
                continue

            hide.append(stack[i])
            numberOfHidden += 1

    return hide


def test1(stack, hidePlayers = False):
    hide = []

    numberOf1x1Npcs = 0
    binaryNumberOfInvisible1x1Npcs = 0
    binaryNumberOfPlayers = 0

    startCounting = BIG_NPC_STACK_DRAW_LIMIT

    for i in range(len(stack)):
        if stack[i][1] == "P" and binaryNumberOfPlayers == 0:
            binaryNumberOfPlayers += 1
            startCounting += 1
        elif stack[i][1] == "N" and int(stack[i][0]) == 1: # 1x1 NPC
            numberOf1x1Npcs += 1
            if i >= startCounting and binaryNumberOfInvisible1x1Npcs == 0:
                binaryNumberOfInvisible1x1Npcs += 1

    numberToHide = len(stack) - BIG_NPC_STACK_DRAW_LIMIT - numberOf1x1Npcs + binaryNumberOfInvisible1x1Npcs

    for i in range(len(stack)):
        if numberToHide <= 0:
            break
        if stack[i][1] == "P" and not hidePlayers:
            continue
        hide.append(stack[i])
        numberToHide -= 1

    return hide


def test(stack, hidePlayers = False):
    hide = []

    numberOfPlayers = 0
    numberOfVisible1x1Npcs = 0
    numberOfInvisible1x1Npcs = 0

    stopCounting = BIG_NPC_STACK_DRAW_LIMIT

    for i in range(len(stack)):
        if stack[i][1] == "P": # Player
            if numberOfPlayers > 0:
                stopCounting += 1
            numberOfPlayers += 1
        elif stack[i][1] == "N" and int(stack[i][0]) == 1: # 1x1 NPC
            if i < stopCounting:
                if numberOfVisible1x1Npcs > 0:
                    stopCounting += 1
                numberOfVisible1x1Npcs += 1
            else:
                numberOfInvisible1x1Npcs += 1

    extraNumberOfInvisible1x1Npcs = max(0, 0 if numberOfVisible1x1Npcs > 0 else (numberOfInvisible1x1Npcs - 1))
    extraNumberOfInvisible1x1Npcs = max(0, (numberOfInvisible1x1Npcs + (0 if numberOfInvisible1x1Npcs == 0 else (
        numberOfVisible1x1Npcs - 1))) if numberOfVisible1x1Npcs > 0 else (numberOfInvisible1x1Npcs - 1)) # TODO: this makes no sense

    show = set()
    if not hidePlayers:
        for i in range(len(stack)):
            if stack[i][1] == "P":
                show.add(stack[i])

    binaryNumberOfPlayers = 0 if hidePlayers else min(1, numberOfPlayers)
    # binaryNumberOfPlayers = (1 if len(show) > 0 else 0)

    i = len(stack) - 1
    n = len(stack) - BIG_NPC_STACK_DRAW_LIMIT + binaryNumberOfPlayers - extraNumberOfInvisible1x1Npcs
    print("len stack =", len(stack), "  binNumPlayers =", binaryNumberOfPlayers,
          "  extraNumInvis1x1 =", extraNumberOfInvisible1x1Npcs,
          "  numInvis1x1 =", numberOfInvisible1x1Npcs,"  numVis1x1 =", numberOfVisible1x1Npcs)
    while i >= max(n, 0):
        #if stack[i][1] == "N" and int(stack[i][0]) == 1:
        #    if numberOfVisible1x1Npcs == 0 and numberOfInvisible1x1Npcs > 0:
        #        n -= 1
        #    numberOfInvisible1x1Npcs += 1
        show.add(stack[i])
        i -= 1

    for i in range(len(stack)):
        if stack[i] not in show:
            hide.append(stack[i])

    return hide


if __name__ == "__main__":
    # 1P1 = 1x1 Player number 1
    # 1N2 = 1x1 NPC number 2
    # 2N3 = 2x2 NPC number 3

    assert test(["1P1", "2N1", "2N2", "2N3", "2N4", "2N5"]) == ["2N1"] #, test(["1P1", "2N1", "2N2", "2N3", "2N4", "2N5"])
    assert test(["1P1", "2N1", "2N2", "2N3", "2N4", "2N5"], True) == ["1P1"]
    assert test(["1P1", "2N1", "2N2", "2N3", "2N4", "2N5", "2N6"]) == ["2N1", "2N2"]
    assert test(["1P1", "2N1", "2N2", "2N3", "2N4", "2N5", "2N6"], True) == ["1P1", "2N1"]

    assert test(["1N1", "2N1", "2N2", "2N3", "2N4", "2N5"]) == ["1N1"], test(["1N1", "2N1", "2N2", "2N3", "2N4", "2N5"])
    assert test(["1N1", "2N1", "2N2", "2N3", "2N4", "2N5", "2N6"]) == ["1N1", "2N1"]
    assert test(["1N1", "1N2", "2N1", "2N2", "2N3", "2N4", "2N5"]) == ["1N1", "1N2"], test(["1N1", "1N2", "2N1", "2N2", "2N3", "2N4", "2N5"])
    assert test(["1N1", "1N2", "2N1", "2N2", "2N3", "2N4", "2N5", "2N6"]) == ["1N1", "1N2", "2N1"]

    assert test(["2N1", "2N2", "2N3", "2N4", "1N1"]) == []
    assert test(["2N1", "2N2", "2N3", "2N4", "2N5", "1N1"]) == ["2N1"]
    assert test(["2N1", "2N2", "2N3", "2N4", "2N5", "1N1", "1N2"]) == ["2N1"], test(["2N1", "2N2", "2N3", "2N4", "2N5", "1N1", "1N2"])
    assert test(["2N1", "2N2", "2N3", "2N4", "1N1", "1N2", "1N3"]) == [], test(["2N1", "2N2", "2N3", "2N4", "1N1", "1N2", "1N3"])
    assert test(["1N1", "2N1", "2N2", "2N3", "2N4", "1N2", "1N3"]) == [], test(["1N1", "2N1", "2N2", "2N3", "2N4", "1N2", "1N3"])
    assert test(["1N1", "1N2", "2N1", "2N2", "2N3", "2N4", "1N3", "1N4"]) == [], test(["1N1", "1N2", "2N1", "2N2", "2N3", "2N4", "1N3", "1N4"])
    assert test(["1N1", "1N2", "2N1", "2N2", "2N3", "2N4", "2N5", "1N3", "1N4"]) == ["1N1", "1N2", "2N1"], test(["1N1", "1N2", "2N1", "2N2", "2N3", "2N4", "2N5", "1N3", "1N4"])

    # TODO: assert test(["1N1", "2N1", "2N2", "2N3", "2N4", "2N5", "1N2", "1N3"], prioritizeHiding1x1Npcs = True) == ["1N1", "1N2", "1N3"]
    assert test(["1N1", "2N1", "2N2", "2N3", "2N4", "2N5", "1N2", "1N3"]) == ["1N1", "2N1"]

    assert test(["1N1", "1N2", "1N3", "1N4", "1N5"]) == []
    assert test(["1N1", "1N2", "1N3", "1N4", "1N5", "1N6"]) == []
    assert test(["1N1", "1N2", "1N3", "1N4", "1N5", "1N6", "1N7"]) == []

    assert test(["1N1", "1N2", "1N3", "1N4", "1N5", "2N1"]) == []
    assert test(["1N1", "1N2", "1N3", "1N4", "1N5", "2N1", "2N2"]) == []
    assert test(["1N1", "1N2", "1N3", "1N4", "1N5", "2N1", "2N2", "2N3"]) == []
    assert test(["1N1", "1N2", "1N3", "1N4", "1N5", "2N1", "2N2", "2N3", "2N4"]) == []
    assert test(["1N1", "1N2", "1N3", "1N4", "1N5", "2N1", "2N2", "2N3", "2N4", "2N5"]) == ["1N1", "1N2", "1N3", "1N4", "1N5"]

    assert test(["1P1", "1N1", "2N1", "2N2", "2N3", "2N4"]) == ["1N1"]
    assert test(["1P1", "1N1", "2N1", "2N2", "2N3", "2N4", "2N5"]) == ["1N1", "2N1"]
    assert test(["1P1", "1N1", "2N1", "2N2", "2N3", "2N4", "2N5"], True) == ["1P1", "1N1"]

    assert test(["1P1", "1N1", "2N1", "2N2", "2N3", "2N4", "2N5", "2N6", "2N7", "2N8"]) == ["1N1", "2N1", "2N2", "2N3", "2N4"]
    assert test(["1P1", "1N1", "2N1", "2N2", "2N3", "2N4", "2N5", "2N6", "2N7", "2N8"], True) == ["1P1", "1N1", "2N1", "2N2", "2N3"]

    """
    [P1 2N1 2N2 2N3 2N4 2N5] -> hide [P1] or nothing     ???? [2N1]
    [P1 2N1 2N2 2N3 2N4 2N5 2N6] -> hide [P1 2N1] or [2N1]   ??? [2N1 2N2]
    -
    [1N1 2N1 2N2 2N3 2N4 2N5] -> hide [1N1]
    [1N1 2N1 2N2 2N3 2N4 2N5 2N6] -> hide [1N1 2N1]
    [1N1 1N2 2N1 2N2 2N3 2N4 2N5] -> hide [1N1 1N2]
    [1N1 1N2 2N1 2N2 2N3 2N4 2N5 2N6] -> hide [1N1 1N2 2N1]
    -
    [2N1 2N2 2N3 2N4 1N1] -> hide nothing
    [2N1 2N2 2N3 2N4 2N5 1N1] -> hide [2N1]   double check this?
    [2N1 2N2 2N3 2N4 2N5 1N1 1N2] -> hide [2N1]   double check this?
    -
    [1N1 1N2 1N3 1N4 1N5] -> hide nothing
    [1N1 1N2 1N3 1N4 1N5 1N6] -> hide nothing
    [1N1 1N2 1N3 1N4 1N5 1N6 1N7] -> hide nothing
    -
    [1N1 1N2 1N3 1N4 1N5 2N1] -> hide nothing
    [1N1 1N2 1N3 1N4 1N5 2N1 2N2] -> hide nothing
    [1N1 1N2 1N3 1N4 1N5 2N1 2N2 2N3] -> hide nothing
    [1N1 1N2 1N3 1N4 1N5 2N1 2N2 2N3 2N4] -> hide nothing
    [1N1 1N2 1N3 1N4 1N5 2N1 2N2 2N3 2N4 2N5] -> hide [1N1 1N2 1N3 1N4 1N5]
    -
    [P1 1N1 2N1 2N3 2N4 2N5] -> hide [1N1]
    [P1 1N1 2N1 2N3 2N4 2N5 2N6] -> hide [P1 1N1] or [1N1]
    """
    
    """
    NPC stack (bottom to top) and runelite npc array order:

	[1x1_Goblin, 2x2_Cow1, 2x2_Cow2, 2x2_Cow3, 2x2_Cow4, 2x2_Cow5, 1x1_Farmer]
	Without plugin the 2x2_Cow5 turns invisible in the stack

	Then I removed 1x1_Goblin from the stack
	
	[2x2_Cow1, 2x2_Cow2, 2x2_Cow3, 2x2_Cow4, 2x2_Cow5, 1x1_Farmer]
	Without plugin the 1x1_Farmer turns invisible in the stack
	
	[1x1_Goblin, 1x1_Farmer, 2x2_Cow1, 2x2_Cow2, 2x2_Cow3, 2x2_Cow4, 2x2_Cow5]
	Without plugin the 2x2_Cow5 turns invisible in the stack,
	but the 2x2_Cow4 does not turn invisible => 1x1_Goblin and 1x1_Farmer counts as 1
    """
