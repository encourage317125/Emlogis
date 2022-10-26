SELECT        t .[SiteID], c.RestrictionName, c.MaxValue, c.MinValue, c.RestrictionOrigin
FROM            T_Site t CROSS apply
                             (SELECT        [SiteId], 'ConsecutiveDays', t .EmployeeConsecutiveDays, 0, 'SiteContract'
                               UNION ALL
                              SELECT        [SiteId], 'ShiftOverlap', t .BackToBack, 0, 'SiteContract'
                               UNION ALL
                               SELECT        NULL, 'HoursBetweenShifts', 0, t .HoursOffBetweenDays, 'SiteContract') c([SiteID], RestrictionName, MaxValue, MinValue, RestrictionOrigin)