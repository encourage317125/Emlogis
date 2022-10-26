SELECT        dbo.T_Shift.ShiftID, dbo.T_Shift.Description AS ShiftDescription, dbo.T_Shift.StartTime AS StartingTime, dbo.T_Shift.EndTime AS EndingTime, dbo.T_Shift.ShiftType, 
                         dbo.T_SiteTeam.TeamID, dbo.T_Skill.Name AS SkillName, dbo.T_Skill.SkillID, dbo.T_SiteShift.SiteID, dbo.T_SiteShiftStructure.SiteShiftStructureID, 
                         dbo.T_SiteDemand.Date
FROM            dbo.T_SiteSkill INNER JOIN
                         dbo.T_SiteTeam INNER JOIN
                         dbo.T_SiteTeamSkill ON dbo.T_SiteTeam.SiteTeamID = dbo.T_SiteTeamSkill.SiteTeamID ON dbo.T_SiteSkill.SiteSkillID = dbo.T_SiteTeamSkill.SiteSkillID INNER JOIN
                         dbo.T_Skill ON dbo.T_SiteSkill.SkillID = dbo.T_Skill.SkillID INNER JOIN
                         dbo.T_SiteDemand ON dbo.T_SiteTeamSkill.SiteTeamSkillID = dbo.T_SiteDemand.SiteTeamSkillID INNER JOIN
                         dbo.T_Shift INNER JOIN
                         dbo.T_SiteShift ON dbo.T_Shift.ShiftID = dbo.T_SiteShift.ShiftID INNER JOIN
                         dbo.T_SiteShiftStructure ON dbo.T_SiteShift.SiteShiftID = dbo.T_SiteShiftStructure.SiteShiftID ON 
                         dbo.T_SiteDemand.SiteDemandID = dbo.T_SiteShiftStructure.SiteDemandID