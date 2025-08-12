return function(args)
    local sectorPos = args[1]
    local sectorType = args[2]
    local forced = args[3] or false
    local starColor = args[4]

    local entityGenTable = {}
    if((sectorType == "planet" and math.random() < 0.1) or forced) then
        entityGenTable[1] = EntityGenData:new("ASD-Frigate", "Destroyed Freighter", ItemUtils:getRandomStacks(1, 5, "general"))
        entityGenTable[2] = PirateGenData:new("Foxglove Class Mark IV", "Foxglove Class Mark IV", {})
    end
    return entityGenTable
end