return function()
    local lootTable = {
        ItemStack:new("ship_core", 1),
        LogBook:new("Captain's Log [10.5.3122]:\n\nWe encountered another strange anomaly today. It appears to be a small capsule, the size of a child. Yet it's radar signature would indicate that of a colossal battleship, gigantic in size.\nEither case, we opened it easily, but found nothing.\nNoting some strange tear marks and dents across the surface, our chief engineer hypothesized that something was desperately trying to get into it... or perhaps get out of it.\nStrangely enough, the massive radar signature we had detected earlier disappeared after we opened it.")
    }
    local entityGenTable = {}
    if math.random() < 0.3 then
        entityGenTable[1] = EntityGenData:new("test_ship", "test_ship", lootTable)
    end
    return entityGenTable
end