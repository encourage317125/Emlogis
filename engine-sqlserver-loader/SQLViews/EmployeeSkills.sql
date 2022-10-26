SELECT        dbo.T_SiteSkill.SkillID, dbo.T_SiteEmployee.SiteID, dbo.T_SiteEmployee.EmployeeID, dbo.T_SiteEmployeeSkill.SiteSkillID, dbo.T_SiteEmployeeSkill.IsPrimary, 
                         dbo.T_Skill.Name, dbo.T_SiteTeam.TeamID
FROM            dbo.T_SiteEmployee INNER JOIN
                         dbo.T_SiteEmployeeSkill ON dbo.T_SiteEmployee.SiteEmployeeID = dbo.T_SiteEmployeeSkill.SiteEmployeeID INNER JOIN
                         dbo.T_SiteSkill ON dbo.T_SiteEmployeeSkill.SiteSkillID = dbo.T_SiteSkill.SiteSkillID INNER JOIN
                         dbo.T_Skill ON dbo.T_SiteSkill.SkillID = dbo.T_Skill.SkillID INNER JOIN
                         dbo.T_SiteTeamSkill ON dbo.T_SiteSkill.SiteSkillID = dbo.T_SiteTeamSkill.SiteSkillID INNER JOIN
                         dbo.T_SiteTeam ON dbo.T_SiteTeamSkill.SiteTeamID = dbo.T_SiteTeam.SiteTeamID