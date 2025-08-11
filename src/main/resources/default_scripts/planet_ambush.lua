return function(args)
    local sectorPos = args[1]
    local sectorType = args[2]
    local forced = args[3] or false
    local starColor = args[4]

    local entityGenTable = {}
    if sectorType == "planet" then
        if math.random() < 0.1 or forced then
            entityGenTable[1] = EntityGenData:new("Foxglove Class Mark IV", "Foxglove Class Mark IV", ItemUtils:getRandomStacks(1, 5, "general"))
            entityGenTable[2] = EntityGenData:new("ASD-Frigate", "ASD-Frigate", {})
        end
    end
    return entityGenTable
end